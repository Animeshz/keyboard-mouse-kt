package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cstr
import kotlinx.cinterop.free
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import platform.posix.RTLD_GLOBAL
import platform.posix.RTLD_LAZY
import platform.posix.SIGINT
import platform.posix.dlclose
import platform.posix.dlopen
import platform.posix.dlsym
import platform.posix.exit
import platform.posix.getenv
import platform.posix.on_exit
import platform.posix.signal
import x11.DisplayVar
import x11.XEvent
import x11.XGenericEventCookie
import x11.XIEventMask
import x11.XIRawEvent
import x11.XKeyEvent
import x11.XKeyboardState

@Suppress("PrivatePropertyName")
@ExperimentalUnsignedTypes
@ExperimentalKeyIO
internal class X11KeyboardHandler(
        x11: COpaquePointer,
        xInput2: COpaquePointer,
) : NativeKeyboardHandler {
    private val worker: Worker = Worker.start(errorReporting = true, name = "X11KeyboardHandler")
    private val display: CPointer<DisplayVar>
    private val xiOpcode: Int
    private val eventsInternal: MutableSharedFlow<KeyEvent> = MutableSharedFlow(extraBufferCapacity = 8)

    /**
     * A [SharedFlow] of [KeyEvent] for receiving Key events from the target platform.
     */
    override val events: SharedFlow<KeyEvent>
        get() = eventsInternal.asSharedFlow()

    override fun sendEvent(keyEvent: KeyEvent) {
        if (keyEvent.key == Key.Unknown) return

        memScoped {
            val focusedWindow = alloc<ULongVar>()
            val focusRevert = alloc<IntVar>()
            val mask = if (keyEvent.state == KeyState.KeyDown) KEY_PRESS_MASK else KEY_RELEASE_MASK

            XGetInputFocus(display, focusedWindow.ptr, focusRevert.ptr)
            val event = alloc<XKeyEvent>().apply {
                keycode = (keyEvent.key.keyCode + 8).toUInt()
                type = if (keyEvent.state == KeyState.KeyDown) KEY_PRESS else KEY_RELEASE
                root = focusedWindow.value
                this.display = display
            }

            XSendEvent(display, focusedWindow.value, 1, mask, event.ptr.reinterpret())
        }
    }

    override fun getKeyState(key: Key): KeyState {
        if (key == Key.Unknown) return KeyState.KeyUp

        memScoped {
            val keyStates = allocArray<ByteVar>(32)
            XQueryKeymap(display, keyStates)
            val xKeyCode = key.keyCode + 8
            return if (keyStates[xKeyCode / 8].toInt() and (1 shl xKeyCode % 8) != 0) KeyState.KeyDown
            else KeyState.KeyUp
        }
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
    private val XQueryExtension =
            resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<ByteVar>, CValuesRef<IntVar>, CValuesRef<IntVar>, CValuesRef<IntVar>) -> Int>(
                    x11, "XQueryExtension"
            )

    private val XSync = resolveDlFun<(CValuesRef<DisplayVar>, Int) -> Int>(x11, "XSync")
    private val XQueryKeymap = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<ByteVar>) -> Int>(x11, "XQueryKeymap")
    private val XNextEvent = resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<XEvent>) -> Int>(x11, "XNextEvent")
    private val XSendEvent =
            resolveDlFun<(CValuesRef<DisplayVar>, ULong, Int, Long, CValuesRef<XEvent>) -> Int>(x11, "XSendEvent")

    private val XFreeEventData =
            resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<XGenericEventCookie>) -> Unit>(x11, "XFreeEventData")
    private val XGetEventData =
            resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<XGenericEventCookie>) -> Int>(x11, "XGetEventData")
    private val XGetInputFocus =
            resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<ULongVar>, CValuesRef<IntVar>) -> Int>(
                    x11, "XGetInputFocus"
            )
    private val XGetKeyboardControl =
            resolveDlFun<(CValuesRef<DisplayVar>, CValuesRef<XKeyboardState>) -> Int>(x11, "XGetKeyboardControl")
    private val XISelectEvents =
            resolveDlFun<(CValuesRef<DisplayVar>, ULong, CValuesRef<XIEventMask>, Int) -> Int>(
                    xInput2, "XISelectEvents"
            )

    init {
        val unconfinedScope = CoroutineScope(Dispatchers.Unconfined)
        // When subscriptionCount increments from 0 to 1, setup the native hook.
        eventsInternal.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                .filter { it }
                .onEach {
                    worker.execute(mode = TransferMode.SAFE, { this }) { handler -> handler.readEvents() }
                }
                .launchIn(unconfinedScope)

        display = XOpenDisplay(null) ?: throw RuntimeException("X11 connection can't be established")

        val xiOpcode = nativeHeap.alloc<IntVar>()
        val queryEvent = nativeHeap.alloc<IntVar>()
        val queryError = nativeHeap.alloc<IntVar>()
        XQueryExtension(display, "XInputExtension".cstr, xiOpcode.ptr, queryEvent.ptr, queryError.ptr)
        this.xiOpcode = xiOpcode.value
        nativeHeap.free(xiOpcode)
        nativeHeap.free(queryEvent)
        nativeHeap.free(queryError)

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

        // Force execute cleanup handlers on SIGINT (Ctrl + C)
        signal(SIGINT, staticCFunction { _ -> exit(0) })

        @Suppress("UNCHECKED_CAST")
        on_exit(staticCFunction { _, argsPtr ->
            val argsStableRef = argsPtr!!.asStableRef<List<Any>>()
            val args = argsStableRef.get()
            (args[3] as CoroutineScope).cancel()
            (args[4] as Worker).requestTermination()

            @Suppress("LocalVariableName")
            val XCloseDisplay =
                    resolveDlFun<(CValuesRef<DisplayVar>) -> Int>(args[0] as COpaquePointer, "XCloseDisplay")

            XCloseDisplay(args[2] as CValuesRef<DisplayVar>)
            dlclose(args[0] as COpaquePointer)
            dlclose(args[1] as COpaquePointer)

            argsStableRef.dispose()
        }, StableRef.create(listOf(x11, xInput2, display, unconfinedScope, worker)).asCPointer())
    }

    private fun readEvents() {
        memScoped {
            val event = alloc<XEvent>()

            while (eventsInternal.subscriptionCount.value != 0) {
                XNextEvent(display, event.ptr)
                val cookie = event.xcookie
                if (cookie.type != GENERIC_EVENT || cookie.extension != xiOpcode) continue

                if (XGetEventData(display, cookie.ptr) != 0) {
                    val keyEventType = when (cookie.evtype) {
                        XI_RAW_KEY_PRESS -> KeyState.KeyDown
                        XI_RAW_KEY_RELEASE -> KeyState.KeyUp
                        else -> continue
                    }
                    val cookieData = cookie.data!!.reinterpret<XIRawEvent>().pointed
                    process(keyEventType, cookieData.detail - 8)
                }

                XFreeEventData(display, cookie.ptr)
            }
        }
    }

    /**
     * Processes the event.
     */
    private fun process(keyState: KeyState, code: Int) {
        val key = Key.fromKeyCode(code)
        eventsInternal.tryEmit(KeyEvent(key, keyState))
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
            val xInput2 = dlopen("libXi.so.6", RTLD_GLOBAL or RTLD_LAZY) ?: return null

            // Check XInput2 functions are present, since libXi may contain XInput or XInput2.
            dlsym(xInput2, "XISelectEvents") ?: return null

            return X11KeyboardHandler(x11, xInput2)
        }

        private inline fun <T : Function<*>> resolveDlFun(
                handle: COpaquePointer,
                name: String
        ): CPointer<CFunction<T>> =
                resolveDlPtr(handle, name)

        @Suppress("UNCHECKED_CAST")
        private inline fun <T : CPointed> resolveDlPtr(handle: COpaquePointer, name: String): CPointer<T> =
                dlsym(handle, name) as CPointer<T>? ?: throw NoSuchElementException(name)
    }
}