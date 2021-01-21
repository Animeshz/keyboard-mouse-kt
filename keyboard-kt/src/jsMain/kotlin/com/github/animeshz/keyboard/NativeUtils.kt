package com.github.animeshz.keyboard

public external fun require(module: String): dynamic

internal object NativeUtils {
    private val suffix = when(val architecture = arch()) {
        "x64" -> "X64"
        "x32" -> "X86"
        else -> error("Non x86 architectures are not supported, current architecture: $architecture")
    }
    private val identifier = when(val platform = platform()) {
        "darwin" -> error("Mac os is currently not supported")
        "linux" -> "Linux"
        "win32" -> "Windows"
        else -> error("OS not supported. Current OS: $platform")
    }

    @ExperimentalKeyIO
    val nApiNativeHandler: NApiNativeHandler =
        (require("./lib/KeyboardKt$identifier$suffix.node").NApiNativeHandler() as NApiNativeHandler)
            .also { it.init() }
}

@ExperimentalKeyIO
internal external class NApiNativeHandler {
    fun send(scanCode: Int, isPressed: Boolean)
    fun isPressed(scanCode: Int): Boolean

    fun isCapsLockOn(): Boolean
    fun isNumLockOn(): Boolean
    fun isScrollLockOn(): Boolean

    fun init(): Int
    fun startReadingEvents(handler: (scanCode: Int, isPressed: Boolean) -> Unit): Int
    fun stopReadingEvents()
}
