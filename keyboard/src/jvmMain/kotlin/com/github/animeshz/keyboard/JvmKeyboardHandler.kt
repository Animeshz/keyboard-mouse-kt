package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState

@ExperimentalKeyIO
internal object JvmKeyboardHandler : NativeKeyboardHandlerBase() {
    override fun sendEvent(keyEvent: KeyEvent) {
        TODO("Not yet implemented")
    }

    override fun getKeyState(key: Key): KeyState {
        TODO("Not yet implemented")
    }

    override fun readEvents() {
        TODO("Not yet implemented")
    }

    external override fun isCapsLockOn(): Boolean

    external override fun isNumLockOn(): Boolean

    external override fun isScrollLockOn(): Boolean

    init {
        NativeUtils.loadLibraryFromJar("KeyboardKt")
    }
}

@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return JvmKeyboardHandler
}
