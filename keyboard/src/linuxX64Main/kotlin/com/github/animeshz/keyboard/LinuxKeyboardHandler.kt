package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.AtomicNativePtr
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.internal.NativePtr
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import platform.posix.getenv
import x11.BadRequest
import x11.Display
import x11.GenericEvent
import x11.KeyPress
import x11.KeyPressMask
import x11.KeyRelease
import x11.KeyReleaseMask
import x11.Success
import x11.True
import x11.XCloseDisplay
import x11.XDefaultRootWindow
import x11.XEvent
import x11.XFreeEventData
import x11.XGetEventData
import x11.XGetInputFocus
import x11.XIAllMasterDevices
import x11.XIEventMask
import x11.XIQueryVersion
import x11.XIRawEvent
import x11.XISelectEvents
import x11.XI_LASTEVENT
import x11.XI_RawKeyPress
import x11.XI_RawKeyRelease
import x11.XKeyEvent
import x11.XNextEvent
import x11.XOpenDisplay
import x11.XQueryExtension
import x11.XQueryKeymap
import x11.XSendEvent
import x11.XSync

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
internal object X11KeyboardHandler : NativeKeyboardHandler {
    private val worker: Worker = Worker.start(errorReporting = true, name = "LinuxKeyboardHandler")
    private val connection: AtomicNativePtr = AtomicNativePtr(NativePtr.NULL)
    private val xiOpcode: AtomicInt = AtomicInt(0)
    private val eventsInternal: MutableSharedFlow<KeyEvent> = MutableSharedFlow(extraBufferCapacity = 8)

    /**
     * A [SharedFlow] of [KeyEvent] for receiving Key events from the target platform.
     */
    override val events: SharedFlow<KeyEvent>
        get() = eventsInternal.asSharedFlow()

    init {
        // When subscriptionCount increments from 0 to 1, setup the native hook.
        eventsInternal.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                .filter { it }
                .onEach {
                    worker.execute(mode = TransferMode.SAFE, { this }) { handler ->
                        if (connection.value == NativePtr.NULL) handler.prepare()
                        handler.readEvents()
                        handler.cleanup()
                    }
                }
                .launchIn(CoroutineScope(Dispatchers.Unconfined))
    }

    override fun sendEvent(keyEvent: KeyEvent, moreOnTheWay: Boolean) {
        if (keyEvent.key == Key.Unknown) return
        if (connection.value == NativePtr.NULL) prepare()

        memScoped {
            val display = interpretCPointer<Display>(connection.value)
            val focusedWindow = alloc<ULongVar>()
            val focusRevert = alloc<IntVar>()
            val mask = if (keyEvent.state == KeyState.KeyDown) KeyPressMask else KeyReleaseMask

            XGetInputFocus(display, focusedWindow.ptr, focusRevert.ptr)
            val event = alloc<XKeyEvent>().apply {
                keycode = keyEvent.key.keyCode.toUInt()
                type = if (keyEvent.state == KeyState.KeyDown) KeyPress else KeyRelease
                root = focusedWindow.value
                this.display = display
            }

            XSendEvent(display, focusedWindow.value, True, mask, event.ptr.reinterpret())
        }
        if (!moreOnTheWay) cleanup()
    }

    override fun getKeyState(key: Key): KeyState {
        if (key == Key.Unknown) return KeyState.KeyUp
        if (key == Key.Super) return KeyState.KeyUp  // TODO: Temporarily return KeyUp for Super key, Fix it.

        val display = interpretCPointer<Display>(connection.value)
        memScoped {
            val keyStates = allocArray<ByteVar>(32)
            XQueryKeymap(display, keyStates)

            return if (keyStates[key.keyCode / 8].toInt() and (1 shl key.keyCode % 8) != 0) KeyState.KeyDown
            else KeyState.KeyUp
        }
    }

    // ==================================== Internals ====================================
    private fun prepare() {
        val display = XOpenDisplay(null)?.also { connection.value = it.rawValue }
            ?: throw RuntimeException("X11 connection can't be established")

        xiOpcode.value = getXiOpCode(display)

        memScoped<Unit> {
            val root = XDefaultRootWindow(display)
            val xiMask = alloc<XIEventMask>().apply {
                deviceid = XIAllMasterDevices
                mask_len = XIMaskLen(XI_LASTEVENT)
                mask = allocArray(mask_len)
            }
            XISetMask(xiMask.mask!!, XI_RawKeyPress)
            XISetMask(xiMask.mask!!, XI_RawKeyRelease)
            XISelectEvents(display, root, xiMask.ptr, 1)
            XSync(display, 0)
        }

        connection.value = display.rawValue
    }

    private fun readEvents() {
        memScoped {
            val display = interpretCPointer<Display>(connection.value)
            val event = alloc<XEvent>()

            while (eventsInternal.subscriptionCount.value != 0) {
                XNextEvent(display, event.ptr)
                val cookie = event.xcookie
                if (XGetEventData(
                            display,
                            cookie.ptr
                    ) != 0 && cookie.type == GenericEvent && cookie.extension == xiOpcode.value
                ) {
                    val keyEventType = when (cookie.evtype) {
                        XI_RawKeyPress -> KeyState.KeyDown
                        XI_RawKeyRelease -> KeyState.KeyUp
                        else -> continue
                    }
                    val cookieData = cookie.data!!.reinterpret<XIRawEvent>().pointed
                    process(keyEventType, cookieData.detail - 8)
                }

                XFreeEventData(display, cookie.ptr)
            }
        }
    }

    private fun cleanup() {
        XCloseDisplay(interpretCPointer(connection.value))
        connection.value = NativePtr.NULL
    }

    /**
     * Processes the event.
     */
    private fun process(keyState: KeyState, code: Int) {
        val key = Key.fromKeyCode(code)
        eventsInternal.tryEmit(KeyEvent(key, keyState))
    }

    /**
     * Ensures host has XInput2 and gets the XI op code.
     */
    private fun getXiOpCode(display: CPointer<Display>): Int {
        memScoped {
            val xiOpcode = alloc<IntVar>()
            val queryEvent = alloc<IntVar>()
            val queryError = alloc<IntVar>()
            if (XQueryExtension(display, "XInputExtension", xiOpcode.ptr, queryEvent.ptr, queryError.ptr) == 0) {
                throw RuntimeException("XInput extension is not available")
            }

            val major = alloc<IntVar> { value = 2 }
            val minor = alloc<IntVar> { value = 0 }
            val queryResult = XIQueryVersion(display, major.ptr, minor.ptr)

            if (queryResult == BadRequest) throw RuntimeException("Need XI 2.0 support (got $major.$minor)")
            else if (queryResult != Success) throw RuntimeException("XInput Internal error")

            return xiOpcode.value
        }
    }

    // Redefine preprocessing macro that wasn't generated by c-interop tool
    @Suppress("FunctionName")
    private fun XISetMask(ptr: CPointer<UByteVar>, event: Int) {
        ptr[event ushr 3] = ptr[event ushr 3] or (1 shl (event and 7)).toUByte()
    }

    @Suppress("FunctionName")
    private fun XIMaskLen(event: Int): Int {
        return (event ushr 3) + 1
    }
}

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return when {
        getenv("DISPLAY") != null -> X11KeyboardHandler
        else -> throw RuntimeException("X11 is not present/running in the host.")
    }
}
