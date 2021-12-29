package io.github.animeshz.kbms.keyboard

import io.github.animeshz.kbms.keyboard.entity.Key
import io.github.animeshz.kbms.keyboard.entity.KeyState
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import platform.posix.RTLD_GLOBAL
import platform.posix.RTLD_LAZY
import platform.posix.dlclose
import platform.posix.dlopen
import platform.posix.dlsym
import platform.posix.getenv
import platform.posix.memset
import x11.DisplayVar
import x11.XClientMessageEvent
import x11.XEvent
import x11.XGenericEventCookie
import x11.XIEventMask
import x11.XIRawEvent
import x11.XKeyboardState
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker

@Suppress("PrivatePropertyName")
@ExperimentalUnsignedTypes
@ExperimentalKeyIO
internal class X11KeyboardHandler(x11: COpaquePointer, xInput2: COpaquePointer, xTest: COpaquePointer) : BaseNativeKeyboard {
    override fun sendEvent(keyCode: Int, isPressed: Boolean) {
        // https://stackoverflow.com/a/42020068/11377112
        XTestFakeKeyEvent(display, (keyCode + 8).toUInt(), if (isPressed) 1 else 0, 0UL)
        XFlush(display)
    }

    override fun isPressed(keyCode: Int): Boolean = memScoped {
        val keyStates = allocArray<ByteVar>(32)
        XQueryKeymap(display, keyStates)
        val xKeyCode = keyCode + 8
        return keyStates[xKeyCode / 8].toInt() and (1 shl xKeyCode % 8) != 0
    }

    override fun isCapsLockOn(): Boolean = toggleStates().toInt() and 1 != 0

    override fun isNumLockOn(): Boolean = toggleStates().toInt() and 2 != 0

    /**
     * This always returns false.
     * ScrollLock is not implemented in X11 as: https://stackoverflow.com/a/8429021/11377112
     */
    override fun isScrollLockOn(): Boolean = false

    // ==================================== Internals ====================================
    private val XOpenDisplay = resolveDlFun<(CValuesRef<ByteVar>?) -> CPointer<DisplayVar>?>(x11, "XOpenDisplay")
    private val XDefaultRootWindow = resolveDlFun<(CValuesRef<DisplayVar>) -> ULong>(x11, "XDefaultRootWindow")
    private val XQueryExtension = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<ByteVar>, CValuesRef<IntVar>, CValuesRef<IntVar>, CValuesRef<IntVar>) -> Int>(x11, "XQueryExtension")
    private val XFlush = resolveDlFun<(CValuesRef<DisplayVar>) -> Int>(x11, "XFlush")

    private val XSync = resolveDlFun<(CValuesRef<DisplayVar>, Int) -> Int>(x11, "XSync")
    private val XQueryKeymap = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<ByteVar>) -> Int>(x11, "XQueryKeymap")
    private val XNextEvent = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<XEvent>) -> Int>(x11, "XNextEvent")
    private val XSendEvent = resolveDlFun<(CValuesRef<DisplayVar>, ULong, Int, Long, CValuesRef<XEvent>) -> Int>(x11, "XSendEvent")
    private val XTestFakeKeyEvent = resolveDlFun<(CValuesRef<DisplayVar>, UInt, Int, ULong) -> Int>(xTest, "XTestFakeKeyEvent")

    private val XFreeEventData = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<XGenericEventCookie>) -> Unit>(x11, "XFreeEventData")
    private val XGetEventData = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<XGenericEventCookie>) -> Int>(x11, "XGetEventData")
    private val XGetInputFocus = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<ULongVar>, CValuesRef<IntVar>) -> Int>(x11, "XGetInputFocus")
    private val XGetKeyboardControl = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<XKeyboardState>) -> Int>(x11, "XGetKeyboardControl")
    private val XISelectEvents = resolveDlFun<(CValuesRef<DisplayVar>, ULong, CValuesRef<XIEventMask>, Int) -> Int>(xInput2, "XISelectEvents")

    private val worker: Worker = Worker.start(errorReporting = true, name = "X11KeyboardHandler")
    private val display: CPointer<DisplayVar> = XOpenDisplay(null) ?: throw RuntimeException("X11 connection can't be established")

    private val xiOpcode: Int = memScoped {
        val xiOpcodeVar = alloc<IntVar>()
        val queryEventVar = alloc<IntVar>()
        val queryErrorVar = alloc<IntVar>()

        XQueryExtension(display, "XInputExtension".cstr, xiOpcodeVar.ptr, queryEventVar.ptr, queryErrorVar.ptr)
        xiOpcodeVar.value
    }

    private val stopReading = AtomicInt(0)

    init {
        memScoped<Unit> {
            val root = XDefaultRootWindow(display)
            val xiMask = alloc<XIEventMask>().apply {
                deviceid = XI_ALL_MASTER_DEVICES
                mask_len = XIMaskLen(XI_LAST_EVENT)
                mask = allocArray(mask_len)
            }
            XISetMask(xiMask.mask!!, XI_RAW_KEY_PRESS)
            XISetMask(xiMask.mask!!, XI_RAW_KEY_RELEASE)
            XISelectEvents(display, root, xiMask.ptr, 1)
            XSync(display, 0)
        }
    }

    @Suppress("UNCHECKED_CAST", "LocalVariableName")
    fun startReadingEvents() {
        worker.execute(mode = TransferMode.SAFE, { this }) { handler ->
            handler.stopReading.value = 0
            memScoped {
                val event = alloc<XEvent>()

                while (true) {
                    handler.XNextEvent(handler.display, event.ptr)
                    if (handler.stopReading.value != 0) break

                    val cookie = event.xcookie
                    if (cookie.type != GENERIC_EVENT || cookie.extension != handler.xiOpcode) continue

                    if (handler.XGetEventData(handler.display, cookie.ptr) != 0) {
                        val keyEventType = when (cookie.evtype) {
                            XI_RAW_KEY_PRESS -> KeyState.KeyDown
                            XI_RAW_KEY_RELEASE -> KeyState.KeyUp
                            else -> continue
                        }
                        val cookieData = cookie.data!!.reinterpret<XIRawEvent>().pointed
                        handler.emitEvent(keyEventType, cookieData.detail - 8)
                    }

                    handler.XFreeEventData(handler.display, cookie.ptr)
                }
            }
        }
    }

    fun stopReadingEvents() {
        stopReading.value = 1

        // Send dummy event, so that event loop exits
        memScoped {
            val dummyEvent = alloc<XClientMessageEvent>()
            memset(dummyEvent.ptr, 0, sizeOf<XClientMessageEvent>().toUInt())
            dummyEvent.apply { type = 33; format = 32 }

            XSendEvent(display, XDefaultRootWindow(display), 0, 0, dummyEvent.ptr.reinterpret())
            XFlush(display)
        }
    }

    /**
     * Processes the event.
     */
    private fun emitEvent(keyState: KeyState, code: Int) {
        val key = Key.fromKeyCode(code)
        this.tryEmit(KeyEvent(key, keyState))
    }

    private fun toggleStates(): ULong {
        memScoped {
            val mask = alloc<XKeyboardState>()
            XGetKeyboardControl(display, mask.ptr)

            return mask.led_mask
        }
    }

    // Redefine preprocessing macro that wasn't generated by c-interop tool
    @Suppress("FunctionName")
    private inline fun XISetMask(ptr: CPointer<UByteVar>, event: Int) {
        ptr[event ushr 3] = ptr[event ushr 3] or (1 shl (event and 7)).toUByte()
    }

    @Suppress("FunctionName")
    private inline fun XIMaskLen(event: Int): Int {
        return (event ushr 3) + 1
    }

    companion object {
        private const val GENERIC_EVENT = 35
        private const val KEY_PRESS = 2
        private const val KEY_RELEASE = 3
        private const val KEY_PRESS_MASK = 1L shl 0
        private const val KEY_RELEASE_MASK = 1L shl 1
        private const val XI_LAST_EVENT = 26
        private const val XI_RAW_KEY_PRESS = 13
        private const val XI_RAW_KEY_RELEASE = 14
        private const val XI_ALL_MASTER_DEVICES = 1

        internal fun create(): X11KeyboardHandler? {
            if (getenv("DISPLAY") == null) return null

            val x11 = dlopen("libX11.so.6", RTLD_GLOBAL or RTLD_LAZY) ?: return null
            val xInput2 = dlopen("libXi.so.6", RTLD_GLOBAL or RTLD_LAZY) ?: return close(listOf(x11))
            val xTest = dlopen("libXtst.so.6", RTLD_GLOBAL or RTLD_LAZY) ?: return close(listOf(x11, xInput2))

            // Check XInput2 functions are present, since libXi may contain XInput or XInput2.
            dlsym(xInput2, "XISelectEvents") ?: return close(listOf(x11, xInput2, xTest))

            return X11KeyboardHandler(x11, xInput2, xTest)
        }

        private inline fun close(pointers: List<COpaquePointer>): Nothing? {
            for (ptr in pointers) {
                dlclose(ptr)
            }
            return null
        }

        private inline fun <T : Function<*>> resolveDlFun(handle: COpaquePointer, name: String): CPointer<CFunction<T>> =
            resolveDlPtr(handle, name)

        @Suppress("UNCHECKED_CAST")
        private inline fun <T : CPointed> resolveDlPtr(handle: COpaquePointer, name: String): CPointer<T> =
            dlsym(handle, name) as CPointer<T>? ?: throw NoSuchElementException(name)
    }
}
