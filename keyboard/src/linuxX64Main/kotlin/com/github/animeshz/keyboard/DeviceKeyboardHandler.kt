package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import platform.posix.geteuid

@ExperimentalKeyIO
@ExperimentalUnsignedTypes
internal class DeviceKeyboardHandler : NativeKeyboardHandlerBase() {
    override fun sendEvent(keyEvent: KeyEvent) {
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

    override fun readEvents() {
        TODO("Not yet implemented")
    }

    companion object {
        internal fun create(): DeviceKeyboardHandler? {
            if (geteuid() != 0U) return null

            TODO("Not yet implemented")
        }
    }
}
