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
 * The State of [Key].
 */
@ExperimentalKeyIO
public enum class KeyState {
    /**
     * Type of state when the user lifts their finger off a key on the keyboard.
     */
    KeyUp,

    /**
     * Type of state when the user presses down their finger on a key on the keyboard.
     */
    KeyDown
}

/**
 * The Toggle State of [Key], if it is toggleable otherwise Off is default.
 *
 * Only for [Key.CapsLock], [Key.NumLock] and [Key.ScrollLock].
 */
public enum class KeyToggleState {
    /**
     * The type of state when the toggleable key is in off state.
     */
    Off,

    /**
     * The type of state when the toggleable key is in off state.
     */
    On
}
