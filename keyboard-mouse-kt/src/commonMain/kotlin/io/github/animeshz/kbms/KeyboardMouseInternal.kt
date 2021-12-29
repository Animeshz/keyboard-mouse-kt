package io.github.animeshz.kbms

internal expect object KeyboardMouseInternal {
    public fun init(): Unit
    public fun register(fn: (keyCode: Int, isPressed: Boolean) -> Unit): Unit
    public fun registerMasked(fn: (dx: Int, dy: Int) -> Unit): Unit
    public fun unregister(): Unit
    public fun unregisterMasked(): Unit
    public fun sendEvent(keyCode: Int, isPressed: Boolean): Unit
    public fun isPressed(keyCode: Int): Boolean
    public fun isCapsLockOn(): Boolean
    public fun isNumLockOn(): Boolean
    public fun isScrollLockOn(): Boolean
}