@file:Suppress("EXPERIMENTAL_API_USAGE") // https://youtrack.jetbrains.com/issue/KT-44007

package io.github.animeshz.kbms.keyboard

import io.github.animeshz.kbms.keyboard.entity.Key
import platform.posix.usleep
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

//public actual object Keyboard {
//    private val idCount = AtomicInt(0)
//    private val handlers = mutableMapOf<Int, KeyboardEventHandler>()
//    private val internalKb: BaseNativeKeyboard by lazy {
//        X11KeyboardHandler.create() ?: DeviceKeyboardHandler.create()
//            ?: error("Neither X11 and XInput2 is present nor root access is given, Cannot instantiate NativeKeyboardHandler.")
//    }
//
//    /**
//     * Adds a [KeyboardEventHandler] to the event queue.
//     * Returns unique id of the handler which can be used to cancel the subscription.
//     */
//    public actual fun addEventHandler(handler: KeyboardEventHandler): Int {
//        handlers[idCount.value] = handler
//        return idCount.value++
//    }
//
//    /**
//     * Removes handler having given unique [id]
//     */
//    public actual fun removeEventHandler(id: Int) {
//        handlers.remove(id)
//    }
//
//    /**
//     * Sends the a [Key] event to the host.
//     */
//    public actual fun sendEvent(keyCode: Int, isPressed: Boolean) {
//        if (keyCode !in Key.Esc.keyCode..Key.F24.keyCode) return
//
//        internalKb.sendEvent(keyCode, isPressed)
//    }
//
//    /**
//     * Gets the current key state (if its pressed or not) from the host.
//     *
//     * For toggle states consider using [isCapsLockOn], [isNumLockOn] and [isScrollLockOn] for respective purpose.
//     */
//    public actual fun isPressed(keyCode: Int): Boolean {
//        if (keyCode !in Key.Esc.keyCode..Key.F24.keyCode) return false
//
//        return internalKb.isPressed(keyCode)
//    }
//
//    /**
//     * Returns true if [Key.CapsLock] is toggled to be on.
//     */
//    public actual fun isCapsLockOn(): Boolean = internalKb.isCapsLockOn()
//
//    /**
//     * Returns true if [Key.NumLock] is toggled to be on.
//     */
//    public actual fun isNumLockOn(): Boolean = internalKb.isNumLockOn()
//
//    /**
//     * Returns true if [Key.ScrollLock] is toggled to be on.
//     */
//    public actual fun isScrollLockOn(): Boolean = internalKb.isScrollLockOn()
//}
//
//public interface BaseNativeKeyboard {
//    public fun sendEvent(keyCode: Int, isPressed: Boolean)
//    public fun isPressed(keyCode: Int): Boolean
//    public fun isCapsLockOn(): Boolean
//    public fun isNumLockOn(): Boolean
//    public fun isScrollLockOn(): Boolean
//}

internal actual object KeyboardInternal {
    public fun init(): Unit
    public fun register(fn: (keyCode: Int, isPressed: Boolean) -> Unit): Unit
    public fun unregister(): Unit
    public fun sendEvent(keyCode: Int, isPressed: Boolean): Unit
    public fun isPressed(keyCode: Int): Boolean
    public fun isCapsLockOn(): Boolean
    public fun isNumLockOn(): Boolean
    public fun isScrollLockOn(): Boolean
}