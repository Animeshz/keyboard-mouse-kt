package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.events.KeyEvent
import kotlinx.coroutines.flow.SharedFlow

/**
 * A low-level implementation for handling [KeyEvent]s (sending and receiving).
 */
@ExperimentalKeyIO
public interface NativeKeyboardHandler {
    /**
     * A [SharedFlow] of [KeyEvent] for receiving Key events from the target platform.
     */
    public val events: SharedFlow<KeyEvent>

    /**
     * Sends the [keyEvent] to the platform.
     *
     * If [moreOnTheWay] is true, the resources won't be closed even if [events] subscriptionCount is 0,
     * usually should be true when there are more events to be sent on the way.
     */
    public fun sendEvent(keyEvent: KeyEvent, moreOnTheWay: Boolean = false)

    //    TODO: public fun getKeyState(key: Key)
}

/**
 * Gets the [NativeKeyboardHandler] for the particular platform.
 */
@ExperimentalKeyIO
public expect fun nativeKbHandlerForPlatform(): NativeKeyboardHandler
