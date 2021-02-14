package com.github.animeshz.keyboard

public external fun require(module: String): dynamic

private val suffix = when(val architecture = arch()) {
    "x64" -> "x64"
    "x32" -> "x86"
    else -> error("Non x86 architectures are not supported, current architecture: $architecture")
}
private val identifier = when(val platform = platform()) {
    "darwin" -> error("Mac os is currently not supported")
    "linux" -> "Linux"
    "win32" -> "Windows"
    else -> error("OS not supported. Current OS: $platform")
}

@ExperimentalKeyIO
internal val NApiNativeHandler: INApiNativeHandler =
    require("./KeyboardKt$identifier$suffix.node").unsafeCast<INApiNativeHandler>()

@ExperimentalKeyIO
internal external interface INApiNativeHandler {
    fun send(scanCode: Int, isPressed: Boolean)
    fun isPressed(scanCode: Int): Boolean

    fun isCapsLockOn(): Boolean
    fun isNumLockOn(): Boolean
    fun isScrollLockOn(): Boolean

    fun init(): Number
    fun startReadingEvents(handler: (scanCode: Int, isPressed: Boolean) -> Unit): Number
    fun stopReadingEvents()
}
