package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@ExperimentalKeyIO
@JsExport
public fun interface KeyboardEventHandler {
    public fun handle(id: Int, keyCode: Int, isPressed: Boolean)
}

/**
 * A low-level implementation for handling key events (sending and receiving).
 */
@ExperimentalJsExport
@ExperimentalKeyIO
public expect object NativeKeyboard {
    /**
     * Adds a [KeyboardEventHandler] to the event queue.
     * Returns unique id of the handler which can be used to cancel the subscription.
     */
    public fun addEventHandler(handler: KeyboardEventHandler): Int

    /**
     * Removes handler having given unique [id]
     */
    public fun removeEventHandler(id: Int)

    /**
     * Sends the a [Key] event to the host.
     */
    public fun sendEvent(keyCode: Int, isPressed: Boolean)

    /**
     * Gets the current key state (if its pressed or not) from the host.
     *
     * For toggle states consider using [isCapsLockOn], [isNumLockOn] and [isScrollLockOn] for respective purpose.
     */
    public fun isPressed(keyCode: Int): Boolean

    /**
     * Returns true if [Key.CapsLock] is toggled to be on.
     */
    public fun isCapsLockOn(): Boolean

    /**
     * Returns true if [Key.NumLock] is toggled to be on.
     */
    public fun isNumLockOn(): Boolean

    /**
     * Returns true if [Key.ScrollLock] is toggled to be on.
     */
    public fun isScrollLockOn(): Boolean
}
