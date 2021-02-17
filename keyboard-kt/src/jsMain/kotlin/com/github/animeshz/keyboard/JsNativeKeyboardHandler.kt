package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalKeyIO
internal object KotlinJsNativeKeyboardHandler : NativeKeyboardHandlerBase() {
    init {
        if (NApiNativeHandler.init().toInt() != 0) {
            error("Native initialization failed")
        }
    }

    override fun sendEvent(keyEvent: KeyEvent) {
        NApiNativeHandler.send(keyEvent.key.keyCode, keyEvent.state.isPressed())
    }

    override fun getKeyState(key: Key): KeyState =
        NApiNativeHandler.isPressed(key.keyCode).toKeyState()

    override fun isCapsLockOn(): Boolean = NApiNativeHandler.isCapsLockOn()

    override fun isNumLockOn(): Boolean = NApiNativeHandler.isNumLockOn()

    override fun isScrollLockOn(): Boolean = NApiNativeHandler.isScrollLockOn()

    override fun startReadingEvents() {
        val code = NApiNativeHandler.startReadingEvents { scanCode, isPressed ->
            eventsInternal.tryEmit(KeyEvent(Key.fromKeyCode(scanCode), isPressed.toKeyState()))
        }

        if (code.toInt() != 0) {
            error("Unable to set native hook. Error code: $code")
        }
    }

    override fun stopReadingEvents() = NApiNativeHandler.stopReadingEvents()
}

@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return KotlinJsNativeKeyboardHandler
}

@ExperimentalKeyIO
internal fun String.toKey(): Key = Key.values().first { it.name == this }

@ExperimentalKeyIO
internal fun KeyState.isPressed() = this == KeyState.KeyDown

@ExperimentalKeyIO
internal fun Boolean.toKeyState() = if (this) KeyState.KeyDown else KeyState.KeyUp

@ExperimentalJsExport
@ExperimentalKeyIO
@JsExport
@JsName("JsNativeKeyboardHandler")
public object JsNativeKeyboardHandler {
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @JsName("addHandler")
    public fun addHandler(handler: (key: String, isPressed: Boolean) -> Unit) {
        KotlinJsNativeKeyboardHandler.events.onEach { handler(it.key.name, it.state.isPressed()) }
            .launchIn(scope)
    }

    @JsName("sendEvent")
    public fun sendEvent(key: String, isPressed: Boolean): Unit =
        KotlinJsNativeKeyboardHandler.sendEvent(KeyEvent(key.toKey(), isPressed.toKeyState()))

    @JsName("getKeyState")
    public fun getKeyState(key: String): Boolean =
        KotlinJsNativeKeyboardHandler.getKeyState(key.toKey()).isPressed()

    @JsName("isCapsLockOn")
    public fun isCapsLockOn(): Boolean = KotlinJsNativeKeyboardHandler.isCapsLockOn()

    @JsName("isNumLockOn")
    public fun isNumLockOn(): Boolean = KotlinJsNativeKeyboardHandler.isNumLockOn()

    @JsName("isScrollLockOn")
    public fun isScrollLockOn(): Boolean = KotlinJsNativeKeyboardHandler.isScrollLockOn()
}

