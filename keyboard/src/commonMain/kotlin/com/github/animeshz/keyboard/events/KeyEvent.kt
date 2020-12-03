package com.github.animeshz.keyboard.events

import com.github.animeshz.keyboard.ExperimentalKeyIO
import com.github.animeshz.keyboard.entity.Key

/**
 * When a user presses a key on a hardware keyboard, a [KeyEvent] is sent.
 *
 * @param key The [Key] that is associated with the event.
 * @param type The type of the event (see [KeyEventType]).
 */
@ExperimentalKeyIO
public class KeyEvent(
        public val key: Key,
        public val type: KeyEventType
) {
    override fun toString(): String = "KeyEvent(key=$key, type=$type)"
}

/**
 * The type of Key Event.
 */
@ExperimentalKeyIO
public enum class KeyEventType {
    /**
     * Type of KeyEvent sent when the user lifts their finger off a key on the keyboard.
     */
    KeyUp,

    /**
     * Type of KeyEvent sent when the user presses down their finger on a key on the keyboard.
     */
    KeyDown
}
