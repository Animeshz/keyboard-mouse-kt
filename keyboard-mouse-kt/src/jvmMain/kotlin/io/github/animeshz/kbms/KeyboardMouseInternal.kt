package io.github.animeshz.kbms

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

internal actual object KeyboardMouseInternal {
    init {
        NativeUtils.loadLibraryFromJar("KeyboardMouseKt")
    }

    public actual external fun init(): Unit
    public actual external fun register(fn: (keyCode: Int, isPressed: Boolean) -> Unit): Unit
    public actual external fun unregister(): Unit
    public actual external fun sendEvent(keyCode: Int, isPressed: Boolean): Unit
    public actual external fun isPressed(keyCode: Int): Boolean
    public actual external fun isCapsLockOn(): Boolean
    public actual external fun isNumLockOn(): Boolean
    public actual external fun isScrollLockOn(): Boolean
}

@ExperimentalTime
internal actual fun callAfter(duration: Duration, callback: () -> Unit) {
    Thread.sleep(duration.toLong(DurationUnit.MILLISECONDS))
    callback()
}
