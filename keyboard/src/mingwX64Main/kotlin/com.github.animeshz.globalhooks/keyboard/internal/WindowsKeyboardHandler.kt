package com.github.animeshz.globalhooks.keyboard.internal

import com.github.animeshz.globalhooks.ExperimentalKeyIO
import com.github.animeshz.globalhooks.keyboard.events.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

@ExperimentalKeyIO
public class WindowsKeyboardHandler(public val scope: CoroutineScope) : NativeKeyboardHandler {
    override val events: SharedFlow<KeyEvent>
        get() = TODO("Not yet implemented")

    override fun sendEvent(keyEvent: KeyEvent) {
        TODO("Not yet implemented")
    }
}

@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(scope: CoroutineScope): NativeKeyboardHandler {
    return WindowsKeyboardHandler(scope)
}
