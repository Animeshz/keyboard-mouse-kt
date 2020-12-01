package com.github.animeshz.globalhooks.keyboard.internal

import com.github.animeshz.globalhooks.ExperimentalKeyIO
import com.github.animeshz.globalhooks.keyboard.events.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

/**
 * A low-level implementation for handling key events (sending and receiving).
 */
@ExperimentalKeyIO
public interface NativeKeyboardHandler {
    public val events: SharedFlow<KeyEvent>
    public fun sendEvent(keyEvent: KeyEvent)
}

@ExperimentalKeyIO
public expect fun nativeKbHandlerForPlatform(scope: CoroutineScope): NativeKeyboardHandler
