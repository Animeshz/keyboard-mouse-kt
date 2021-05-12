package io.github.animeshz.keyboard

import platform.posix.geteuid

@ExperimentalKeyIO
@ExperimentalUnsignedTypes
internal class DeviceKeyboardHandler : BaseNativeKeyboard {
    override fun sendEvent(keyCode: Int, isPressed: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isPressed(keyCode: Int): Boolean {
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

    companion object {
        internal fun create(): DeviceKeyboardHandler? {
            if (geteuid() != 0U) return null

            TODO("Not yet implemented")
        }
    }
}
