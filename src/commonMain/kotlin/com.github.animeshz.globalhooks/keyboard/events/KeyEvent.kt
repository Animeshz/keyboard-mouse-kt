package com.github.animeshz.globalhooks.keyboard.events

import com.github.animeshz.globalhooks.ExperimentalKeyIO
import com.github.animeshz.globalhooks.keyboard.entity.Key

/**
 * When a user presses a key on a hardware keyboard, a [KeyEvent] is sent.
 *
 * @param key The [Key] that is associated with the event.
 * @param type The type of the event (see [KeyEventType]).
 */
@ExperimentalKeyIO
class KeyEvent(
        val key: Key,
        val type: KeyEventType
)

/**
 * The type of Key Event.
 */
@ExperimentalKeyIO
enum class KeyEventType {
    /**
     * Type of KeyEvent sent when the user lifts their finger off a key on the keyboard.
     */
    KeyUp,

    /**
     * Type of KeyEvent sent when the user presses down their finger on a key on the keyboard.
     */
    KeyDown
}
