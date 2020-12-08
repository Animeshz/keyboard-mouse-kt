package com.github.animeshz.keyboard.events

import com.github.animeshz.keyboard.ExperimentalKeyIO
import com.github.animeshz.keyboard.entity.Key

/**
 * When a user presses a key on a hardware keyboard, a [KeyEvent] is sent.
 *
 * @param key The [Key] that is associated with the event.
 * @param state The type of the event (see [KeyState]).
 */
@ExperimentalKeyIO
public class KeyEvent(
        public val key: Key,
        public val state: KeyState
) {
    override fun toString(): String = "KeyEvent(key=$key, type=$state)"
}

/**
 * The type of Key Event.
 */
@ExperimentalKeyIO
public enum class KeyState {
    /**
     * Type of KeyEvent sent when the user lifts their finger off a key on the keyboard.
     */
    KeyUp,

    /**
     * Type of KeyEvent sent when the user presses down their finger on a key on the keyboard.
     */
    KeyDown
}
