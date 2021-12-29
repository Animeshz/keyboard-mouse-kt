package io.github.animeshz.kbms

import io.github.animeshz.kbms.Keyboard.EventHandler
import io.github.animeshz.kbms.Keyboard.isCapsLockOn
import io.github.animeshz.kbms.Keyboard.isNumLockOn
import io.github.animeshz.kbms.Keyboard.isScrollLockOn
import io.github.animeshz.kbms.entity.Button
import io.github.animeshz.kbms.entity.Key
import io.github.animeshz.kbms.entity.MouseEvent
import io.github.animeshz.kbms.entity.Position

/**
 * A low-level implementation for handling mouse events (sending and receiving).
 */
public object Mouse {
    /**
     * A keyboard event handler.
     */
    public fun interface EventHandler {
        public fun handle(event: MouseEvent)
    }

    private val handlers: MutableList<EventHandler> = mutableListOf()

    init {
        KeyboardMouseInternal.init()
    }

    /**
     * Adds a [EventHandler] to the event queue.
     * Returns unique id of the handler which can be used to cancel the subscription.
     */
    public fun addEventHandler(handler: EventHandler): Boolean {
        val added = handlers.add(handler)

        if (added && Mouse.handlers.size == 1) {
            KeyboardMouseInternal.registerKb { keyCode, isPressed ->
                val key = Key.fromKeyCode(keyCode)
                for (han in Keyboard.handlers) {
                    han.handle(key, isPressed)
                }
            }
        }

        return added
    }

    /**
     * Removes handler having given instance
     */
    public fun removeEventHandler(handler: EventHandler): Boolean {
        val removed = Mouse.handlers.remove(handler)

        if (Mouse.handlers.size == 0) {
            KeyboardMouseInternal.unregisterKb()
        }

        return removed
    }

    /**
     * Sends the a [Key] event to the host.
     */
    public fun sendEvent(event: MouseEvent): Unit = KeyboardMouseInternal.sendEvent(key.keyCode, isPressed)

    /**
     * Gets the current key state (if its pressed or not) from the host.
     *
     * For toggle states consider using [isCapsLockOn], [isNumLockOn] and [isScrollLockOn] for respective purpose.
     */
    public fun getButtonState(btn: Button): ButtonState = KeyboardMouseInternal.isPressed(key.keyCode)

    /**
     * Gets current position of the mouse.
     */
    public fun getPosition(): Position = KeyboardMouseInternal.isPressed(key.keyCode)
}
