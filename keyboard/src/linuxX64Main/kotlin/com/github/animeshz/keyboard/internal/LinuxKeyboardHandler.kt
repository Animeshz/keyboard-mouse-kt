package com.github.animeshz.keyboard.internal

import com.github.animeshz.keyboard.ExperimentalKeyIO
import com.github.animeshz.keyboard.events.KeyEvent
import kotlinx.coroutines.flow.SharedFlow

@ExperimentalKeyIO
public object LinuxKeyboardHandler : NativeKeyboardHandler {
    override val events: SharedFlow<KeyEvent>
        get() = TODO("Not yet implemented")

    override fun sendEvent(keyEvent: KeyEvent) {
        TODO("Not yet implemented")
    }
}

@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return LinuxKeyboardHandler
}
