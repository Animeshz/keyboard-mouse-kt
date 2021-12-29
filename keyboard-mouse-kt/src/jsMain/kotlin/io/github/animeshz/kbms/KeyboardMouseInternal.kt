@file:Suppress("NON_EXPORTABLE_TYPE")

package io.github.animeshz.kbms

import io.github.animeshz.kbms.keyboard.arch
import io.github.animeshz.kbms.keyboard.platform
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal actual fun callAfter(duration: Duration, callback: () -> Unit) {
    setTimeout(callback, duration.toInt(DurationUnit.MILLISECONDS))
}

// Find a way to load `.node` and export into globals
internal actual external object  KeyboardMouseInternal {
    public actual fun init(): Unit
    public actual fun register(fn: (keyCode: Int, isPressed: Boolean) -> Unit): Unit
    public actual fun registerMasked(fn: (dx: Int, dy: Int) -> Unit): Unit
    public actual fun unregister(): Unit
    public actual fun unregisterMasked(): Unit
    public actual fun sendEvent(keyCode: Int, isPressed: Boolean): Unit
    public actual fun isPressed(keyCode: Int): Boolean
    public actual fun isCapsLockOn(): Boolean
    public actual fun isNumLockOn(): Boolean
    public actual fun isScrollLockOn(): Boolean
}

/* Decrease the code if possible to export the singleton through the NAPI */
public external fun require(module: String): dynamic

private val suffix = when (val architecture = arch()) {
    "x64" -> "x64"
    "x32" -> "x86"
    else -> error("Non x86 architectures are not supported, current architecture: $architecture")
}
private val identifier = when (val platform = platform()) {
    "darwin" -> error("Mac os is currently not supported")
    "linux" -> "Linux"
    "win32" -> "Windows"
    else -> error("OS not supported. Current OS: $platform")
}

internal external fun setTimeout(handler: dynamic, timeout: Int = definedExternally, vararg arguments: Any?): Int

internal val NApiNativeHandler: INApiNativeHandler =
    require("./KeyboardMouseKt$identifier$suffix.node").unsafeCast<INApiNativeHandler>()

internal external interface INApiNativeHandler {
    fun send(keyCode: Int, isPressed: Boolean)
    fun isPressed(keyCode: Int): Boolean

    fun isCapsLockOn(): Boolean
    fun isNumLockOn(): Boolean
    fun isScrollLockOn(): Boolean

    fun nativeStartReadingEvents(handler: (keyCode: Int, isPressed: Boolean) -> Unit): Number
    fun nativeStopReadingEvents()
}
