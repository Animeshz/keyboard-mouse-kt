package com.github.animeshz.keyboard

public external fun require(module: String): dynamic

@JsModule("os")
public external fun arch(): String

@JsModule("os")
public external fun platform(): String

internal object NativeUtils {
    private val suffix = when(val architecture = arch()) {
        "x64" -> "x64"
        "x32" -> "x86"
        else -> error("Non x86 architectures are not supported, current architecture: $architecture")
    }
    private val identifier = when(val platform = platform()) {
        "darwin" -> error("Mac os is currently not supported")
        "linux" -> "linux"
        "win32" -> "windows"
        else -> error("OS not supported. Current OS: $platform")
    }

    @ExperimentalJsExport
    @ExperimentalKeyIO
    fun getNApiNativeHandler(): NApiNativeHandler =
        require("./lib/$identifier$suffix.node") as NApiNativeHandler
}

@ExperimentalKeyIO
@ExperimentalJsExport
internal external class NApiNativeHandler(
    publicHandler: JsKeyboardHandler
) {
    fun send(scanCode: Int, isPressed: Boolean)
    fun isPressed(scanCode: Int): Boolean

    fun isCapsLockOn(): Boolean
    fun isNumLockOn(): Boolean
    fun isScrollLockOn(): Boolean

    fun init(): Int
    fun startReadingEvents(): Int
    fun stopReadingEvents()
}
