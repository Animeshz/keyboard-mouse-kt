package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState

@ExperimentalJsExport
@ExperimentalKeyIO
internal object JsKeyboardHandler : NativeKeyboardHandlerBase() {
    val nApiNativeHandler = NativeUtils.getNApiNativeHandler().also { it.init() }

    override fun sendEvent(keyEvent: KeyEvent) {
        nApiNativeHandler.send(keyEvent.key.keyCode, keyEvent.state == KeyState.KeyDown)
    }

    override fun getKeyState(key: Key): KeyState =
        if (nApiNativeHandler.isPressed(key.keyCode)) KeyState.KeyDown else KeyState.KeyUp

    override fun isCapsLockOn(): Boolean = nApiNativeHandler.isCapsLockOn()

    override fun isNumLockOn(): Boolean = nApiNativeHandler.isNumLockOn()

    override fun isScrollLockOn(): Boolean = nApiNativeHandler.isScrollLockOn()

    override fun startReadingEvents() {
        val code = nApiNativeHandler.startReadingEvents()
        if (code != 0) {
            error("Unable to set native hook. Error code: $code")
        }
    }

    override fun stopReadingEvents() = nApiNativeHandler.stopReadingEvents()
}

@ExperimentalJsExport
@ExperimentalKeyIO
@JsExport
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return JsKeyboardHandler
}
