package io.github.animeshz.keyboard

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

internal external fun setTimeout(handler: dynamic, timeout: Int = definedExternally, vararg arguments: Any?): Int

@ExperimentalKeyIO
internal val NApiNativeHandler: INApiNativeHandler =
    require("./KeyboardMouseKt$identifier$suffix.node").unsafeCast<INApiNativeHandler>()

@ExperimentalKeyIO
internal external interface INApiNativeHandler {
    fun send(keyCode: Int, isPressed: Boolean)
    fun isPressed(keyCode: Int): Boolean

    fun isCapsLockOn(): Boolean
    fun isNumLockOn(): Boolean
    fun isScrollLockOn(): Boolean

    fun nativeStartReadingEvents(handler: (keyCode: Int, isPressed: Boolean) -> Unit): Number
    fun nativeStopReadingEvents()
}
