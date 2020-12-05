package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyEventType
import kotlin.native.concurrent.AtomicNativePtr
import kotlin.native.internal.NativePtr
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
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
internal object X11KeyboardHandler : LinuxKeyboardHandlerBase() {
    private val connection: AtomicNativePtr = AtomicNativePtr(NativePtr.NULL)

    override fun sendEvent(keyEvent: KeyEvent) {
        memScoped {
            val display = interpretCPointer<Display>(connection.value)
            val focusedWindow = alloc<ULongVar>()
            val focusRevert = alloc<IntVar>()
            val mask = if (keyEvent.type == KeyEventType.KeyDown) KeyPressMask else KeyReleaseMask

            XGetInputFocus(display, focusedWindow.ptr, focusRevert.ptr)
            val event = alloc<XKeyEvent>().apply {
                keycode = keyEvent.key.keyCode.toUInt()
                type = if (keyEvent.type == KeyEventType.KeyDown) KeyPress else KeyRelease
                root = focusedWindow.value
                this.display = display
            }

            XSendEvent(display, focusedWindow.value, True, mask, event.ptr.reinterpret())
        }
    }

    override fun prepare() {
        connection.value = XOpenDisplay(null)?.rawValue ?: throw RuntimeException("X11 connection can't be established")
    }

    override fun readEvents() {
        memScoped {
            val event = alloc<XEvent>()
            val display = interpretCPointer<Display>(connection.value)
            while (true) {
                XPeekEvent(display, event.ptr)
                val keyEventType = when (event.type) {
                    KeyPress -> KeyEventType.KeyDown
                    KeyRelease -> KeyEventType.KeyUp
                    else -> continue
                }

                process(keyEventType, event.xkey.keycode.toInt())
            }
        }
    }

    override fun cleanup() {
        XCloseDisplay(interpretCPointer(connection.value))
    }

    /**
     * Processes the event.
     */
    private fun process(keyEventType: KeyEventType, code: Int) {
        val key = Key.fromKeyCode(code)
        eventsInternal.tryEmit(KeyEvent(key, keyEventType))
    }
}
