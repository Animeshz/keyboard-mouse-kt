import native.kbkt.*

internal actual object KeyboardInternal {
    public fun init(): Unit {}
    public fun register(fn: (keyCode: Int, isPressed: Boolean) -> Unit): Unit {

    }
    public fun unregister(): Unit {

    }
    public fun sendEvent(keyCode: Int, isPressed: Boolean): Unit = sendEvent()
    public fun isPressed(keyCode: Int): Boolean = isPressed()
    public fun isCapsLockOn(): Boolean = isCapsLockOn()
    public fun isNumLockOn(): Boolean = isNumLockOn()
    public fun isScrollLockOn(): Boolean = isScrollLockOn()
}