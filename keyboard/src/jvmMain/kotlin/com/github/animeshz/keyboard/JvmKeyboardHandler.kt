package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

@ExperimentalCoroutinesApi
@ExperimentalKeyIO
internal object JvmKeyboardHandler : NativeKeyboardHandlerBase() {
    private val ioScope = CoroutineScope(newSingleThreadContext("JvmKeyboardHandler"))

    init {
        NativeUtils.loadLibraryFromJar("KeyboardKt")
        if (nativeInit() != 0) {
            error("Native initialization failed")
        }
    }

    override fun sendEvent(keyEvent: KeyEvent) {
        if (keyEvent.key == Key.Unknown) return

        nativeSendEvent(keyEvent.key.keyCode, keyEvent.state == KeyState.KeyDown)
    }

    override fun getKeyState(key: Key): KeyState {
        if (key == Key.Unknown) return KeyState.KeyUp

        return if (nativeIsPressed(key.keyCode)) KeyState.KeyDown else KeyState.KeyUp
    }

    external override fun isCapsLockOn(): Boolean
    external override fun isNumLockOn(): Boolean
    external override fun isScrollLockOn(): Boolean

    override fun startReadingEvents() {
        ioScope.launch {
            val code = nativeStartReadingEvents()
            if (code != 0) {
                // Cannot throw, launch will consume it
                IllegalStateException("Unable to set native hook. Error code: $code").printStackTrace()
            }
        }
    }

    override fun stopReadingEvents() {
        nativeStopReadingEvents()
    }

    private external fun nativeInit(): Int
    private external fun nativeSendEvent(scanCode: Int, isDown: Boolean)
    private external fun nativeIsPressed(scanCode: Int): Boolean
    private external fun nativeStartReadingEvents(): Int
    private external fun nativeStopReadingEvents()

    private fun emitEvent(scanCode: Int, pressed: Boolean) {
        eventsInternal.tryEmit(KeyEvent(Key.fromKeyCode(scanCode), if (pressed) KeyState.KeyDown else KeyState.KeyUp))
    }
}

@ExperimentalCoroutinesApi
@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return JvmKeyboardHandler
}
