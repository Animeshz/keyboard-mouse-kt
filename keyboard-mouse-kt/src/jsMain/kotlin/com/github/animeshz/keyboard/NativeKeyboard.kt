package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlin.random.Random

@Suppress("unused")
@ExperimentalJsExport
@ExperimentalKeyIO
@JsExport
public actual object NativeKeyboard {
    private val handlers: MutableMap<Int, KeyboardEventHandler> = mutableMapOf()
    private var id = atomic(0)

    /**
     * Adds a [KeyboardEventHandler] to the event queue.
     * Returns unique id of the handler which can be used to cancel the subscription.
     */
    public actual fun addEventHandler(handler: KeyboardEventHandler): Int {
        handlers[id.value] = handler

        if (handlers.size == 1) {
            val code = NApiNativeHandler.nativeStartReadingEvents { keyCode, isPressed ->
                for ((id, handler_) in handlers) {
                    handler_.handle(id, keyCode, isPressed)
                }
            }
            if (code != 0) {
                error("Unable to set native hook. Error code: $code")
            }
        }

        return id.value++
    }

    /**
     * Removes handler having given unique [id]
     */
    public actual fun removeEventHandler(id: Int) {
        handlers.remove(id)

        if (handlers.isEmpty()) {
            NApiNativeHandler.nativeStopReadingEvents()
        }
    }

    /**
     * Sends the a [Key] event to the host.
     */
    public actual fun sendEvent(keyCode: Int, isPressed: Boolean) {
        if (keyCode !in Key.Esc.keyCode..Key.F24.keyCode) return

        NApiNativeHandler.send(keyCode, isPressed)
    }

    /**
     * Gets the current key state (if its pressed or not) from the host.
     *
     * For toggle states consider using [isCapsLockOn], [isNumLockOn] and [isScrollLockOn] for respective purpose.
     */
    public actual fun isPressed(keyCode: Int): Boolean {
        if (keyCode !in Key.Esc.keyCode..Key.F24.keyCode) return false

        return NApiNativeHandler.isPressed(keyCode)
    }

    /**
     * Returns true if [Key.CapsLock] is toggled to be on.
     */
    public actual fun isCapsLockOn(): Boolean = NApiNativeHandler.isCapsLockOn()

    /**
     * Returns true if [Key.NumLock] is toggled to be on.
     */
    public actual fun isNumLockOn(): Boolean = NApiNativeHandler.isNumLockOn()

    /**
     * Returns true if [Key.ScrollLock] is toggled to be on.
     */
    public actual fun isScrollLockOn(): Boolean = NApiNativeHandler.isScrollLockOn()
}
