package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.util.function.IntSupplier

@ExperimentalCoroutinesApi
@ExperimentalKeyIO
internal object JvmKeyboardHandler : NativeKeyboardHandlerBase() {
    private val ioScope = CoroutineScope(newSingleThreadContext("JvmKeyboardHandler"))

    init {
        NativeUtils.loadLibraryFromJar("KeyboardKt")

        val code = nativeInit()
        if (code != 0) {
            error("Unable to set native hook. Error code: $code")
        }

        Runtime.getRuntime().addShutdownHook(
            Thread {
                unconfinedScope.cancel()
                ioScope.cancel()
                nativeShutdown()
            }
        )
    }

    override fun sendEvent(keyEvent: KeyEvent) {
        if (keyEvent.key == Key.Unknown) return

        nativeSendEvent(keyEvent.key.keyCode, keyEvent.state == KeyState.KeyDown)
    }

    override fun getKeyState(key: Key): KeyState {
        if (key == Key.Unknown) return KeyState.KeyUp

        return if (nativeIsPressed(key.keyCode)) KeyState.KeyDown else KeyState.KeyUp
    }

    override fun readEvents() {
        ioScope.launch { nativeReadEvent { eventsInternal.subscriptionCount.value } }
    }

    external override fun isCapsLockOn(): Boolean
    external override fun isNumLockOn(): Boolean
    external override fun isScrollLockOn(): Boolean

    private external fun nativeInit(): Int
    private external fun nativeShutdown()
    private external fun nativeSendEvent(scanCode: Int, isDown: Boolean)
    private external fun nativeReadEvent(a: IntSupplier)
    private external fun nativeIsPressed(scanCode: Int): Boolean

    private fun emitEvent(scanCode: Int, pressed: Boolean) {
        eventsInternal.tryEmit(KeyEvent(Key.fromKeyCode(scanCode), if (pressed) KeyState.KeyDown else KeyState.KeyUp))
    }
}

@ExperimentalCoroutinesApi
@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return JvmKeyboardHandler
}
