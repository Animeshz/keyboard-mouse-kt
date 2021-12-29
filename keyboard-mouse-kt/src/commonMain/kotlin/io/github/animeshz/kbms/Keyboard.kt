package io.github.animeshz.kbms

import io.github.animeshz.kbms.entity.Key

/**
 * A low-level implementation for handling key events (sending and receiving).
 */
public object Keyboard {
    /**
     * A keyboard event handler.
     */
    public fun interface EventHandler {
        public fun handle(key: Key, isPressed: Boolean)
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

        if (added && handlers.size == 1) {
            KeyboardMouseInternal.registerKb { keyCode, isPressed ->
                val key = Key.fromKeyCode(keyCode)
                for (han in handlers) {
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
        val removed = handlers.remove(handler)

        if (handlers.size == 0) {
            KeyboardMouseInternal.unregisterKb()
        }

        return removed
    }

    /**
     * Sends the a [Key] event to the host.
     */
    public fun sendEvent(key: Key, isPressed: Boolean): Unit = KeyboardMouseInternal.sendEvent(key.keyCode, isPressed)

    /**
     * Gets the current key state (if its pressed or not) from the host.
     *
     * For toggle states consider using [isCapsLockOn], [isNumLockOn] and [isScrollLockOn] for respective purpose.
     */
    public fun isPressed(key: Key): Boolean = KeyboardMouseInternal.isPressed(key.keyCode)

    /**
     * Returns true if [Key.CapsLock] is toggled to be on.
     */
    public fun isCapsLockOn(): Boolean = KeyboardMouseInternal.isCapsLockOn()

    /**
     * Returns true if [Key.NumLock] is toggled to be on.
     */
    public fun isNumLockOn(): Boolean = KeyboardMouseInternal.isNumLockOn()

    /**
     * Returns true if [Key.ScrollLock] is toggled to be on.
     */
    public fun isScrollLockOn(): Boolean = KeyboardMouseInternal.isScrollLockOn()
}
