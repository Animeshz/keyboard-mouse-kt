package io.github.animeshz.keyboard

import io.github.animeshz.keyboard.entity.Key
import kotlin.js.ExperimentalJsExport
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@ExperimentalJsExport
@ExperimentalKeyIO
public actual object NativeKeyboard {
    private val handlers: MutableMap<Int, KeyboardEventHandler> = mutableMapOf()

    init {
        NativeUtils.loadLibraryFromJar("KeyboardMouseKt")
        if (nativeInit() != 0) {
            error("Native initialization failed")
        }
    }

    /**
     * Adds a [KeyboardEventHandler] to the event queue.
     * Returns unique id of the handler which can be used to cancel the subscription.
     */
    public actual fun addEventHandler(handler: KeyboardEventHandler): Int {
        val id = Random.nextInt()
        handlers[id] = handler

        if (handlers.size == 1) {
            val code = nativeStartReadingEvents()
            if (code != 0) {
                error("Unable to set native hook. Error code: $code")
            }
        }

        return id
    }

    /**
     * Removes handler having given unique [id]
     */
    public actual fun removeEventHandler(id: Int) {
        handlers.remove(id)

        if (handlers.isEmpty()) {
            nativeStopReadingEvents()
        }
    }

    public actual fun sendEvent(keyCode: Int, isPressed: Boolean) {
        if (keyCode !in Key.Esc.keyCode..Key.F24.keyCode) return

        nativeSendEvent(keyCode, isPressed)
    }

    public actual fun isPressed(keyCode: Int): Boolean {
        if (keyCode !in Key.Esc.keyCode..Key.F24.keyCode) return false

        return nativeIsPressed(keyCode)
    }

    public actual external fun isCapsLockOn(): Boolean
    public actual external fun isNumLockOn(): Boolean
    public actual external fun isScrollLockOn(): Boolean

    private external fun nativeInit(): Int
    private external fun nativeSendEvent(scanCode: Int, isPressed: Boolean)
    private external fun nativeIsPressed(scanCode: Int): Boolean
    private external fun nativeStartReadingEvents(): Int
    private external fun nativeStopReadingEvents()

    private fun emitEvent(keyCode: Int, isPressed: Boolean) {
        for ((id, handler) in handlers) {
            handler.handle(id, keyCode, isPressed)
        }
    }
}

@ExperimentalTime
internal actual fun callAfter(duration: Duration, callback: () -> Unit) {
    Thread.sleep(duration.toLong(DurationUnit.MILLISECONDS))
    callback()
}
