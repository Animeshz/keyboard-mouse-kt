package io.github.animeshz.keyboard

import io.github.animeshz.keyboard.entity.Key
import io.github.animeshz.keyboard.events.KeyEvent
import io.github.animeshz.keyboard.events.KeyState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A low-level wrapper around [NativeKeyboardHandler] for handling [KeyEvent]s (sending and receiving).
 */
@ExperimentalCoroutinesApi
@ExperimentalKeyIO
public object JNativeKeyboardHandler {
    private val delegate: NativeKeyboardHandler = nativeKbHandlerForPlatform()
    private val eventHandlerScope = CoroutineScope(Dispatchers.Default)

    /**
     * Adds a [EventHandler] to the event queue.
     * Returns a [Job] which can be used to cancel the subscription.
     */
    public fun addEventHandler(handler: EventHandler): Job =
        delegate.events
            .onEach { handler.handle(it) }
            .launchIn(eventHandlerScope)

    /**
     * Sends the [keyEvent] to the host.
     */
    public fun sendEvent(keyEvent: KeyEvent) {
        delegate.sendEvent(keyEvent)
    }

    /**
     * Gets the current key state (if its pressed or not) from the host.
     *
     * For toggle states consider using [isCapsLockOn], [isNumLockOn] and [isScrollLockOn] for respective purpose.
     */
    public fun getKeyState(key: Key): KeyState = delegate.getKeyState(key)

    /**
     * Returns true if [Key.CapsLock] is toggled to be on.
     */
    public fun isCapsLockOn(): Boolean = delegate.isCapsLockOn()

    /**
     * Returns true if [Key.NumLock] is toggled to be on.
     */
    public fun isNumLockOn(): Boolean = delegate.isNumLockOn()

    /**
     * Returns true if [Key.ScrollLock] is toggled to be on.
     */
    public fun isScrollLockOn(): Boolean = delegate.isScrollLockOn()
}
