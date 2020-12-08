package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlin.native.concurrent.AtomicNativePtr
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.internal.NativePtr
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
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
import x11.Display
import x11.KeyPress
import x11.KeyPressMask
import x11.KeyRelease
import x11.KeyReleaseMask
import x11.True
import x11.XCloseDisplay
import x11.XEvent
import x11.XGetInputFocus
import x11.XKeyEvent
import x11.XOpenDisplay
import x11.XPeekEvent
import x11.XSendEvent

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
internal object X11KeyboardHandler : NativeKeyboardHandler {
    private val worker: Worker = Worker.start(errorReporting = true, name = "LinuxKeyboardHandler")
    private val connection: AtomicNativePtr = AtomicNativePtr(NativePtr.NULL)
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

    // ==================================== Internals ====================================
    private fun prepare() {
        connection.value = XOpenDisplay(null)?.rawValue ?: throw RuntimeException("X11 connection can't be established")
    }

    private fun readEvents() {
        memScoped {
            val event = alloc<XEvent>()
            val display = interpretCPointer<Display>(connection.value)
            while (true) {
                XPeekEvent(display, event.ptr)
                val keyEventType = when (event.type) {
                    KeyPress -> KeyState.KeyDown
                    KeyRelease -> KeyState.KeyUp
                    else -> continue
                }

                process(keyEventType, event.xkey.keycode.toInt())
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
}

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return when {
        getenv("DISPLAY") != null -> X11KeyboardHandler
        else -> throw RuntimeException("X11 is not present/running in the host.")
    }
}
