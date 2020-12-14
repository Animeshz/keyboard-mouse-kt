package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.coroutines.flow.SharedFlow

@ExperimentalKeyIO
internal object JvmKeyboardHandler : NativeKeyboardHandler {
    override val events: SharedFlow<KeyEvent>
        get() = TODO("Not yet implemented")

    override fun sendEvent(keyEvent: KeyEvent, moreOnTheWay: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getKeyState(key: Key): KeyState {
        TODO("Not yet implemented")
    }

    override fun isCapsLockOn(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isNumLockOn(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isScrollLockOn(): Boolean {
        TODO("Not yet implemented")
    }
}

@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return JvmKeyboardHandler
}
