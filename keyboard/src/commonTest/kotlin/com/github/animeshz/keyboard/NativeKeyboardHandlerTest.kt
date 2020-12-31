package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import io.kotest.matchers.comparables.shouldNotBeEqualComparingTo
import kotlin.test.Test

@ExperimentalKeyIO
class NativeKeyboardHandlerTest {
    @Test
    fun `Caps lock key should be toggled when KeyDown event is triggered`() {
        val handler = nativeKbHandlerForPlatform()

        val initialState = handler.isCapsLockOn()

        handler.sendEvent(KeyEvent(Key.CapsLock, KeyState.KeyDown))
        handler.sendEvent(KeyEvent(Key.CapsLock, KeyState.KeyUp))

        val finalState = handler.isCapsLockOn()

        // Set the state back to initialState
        handler.sendEvent(KeyEvent(Key.CapsLock, KeyState.KeyDown))
        handler.sendEvent(KeyEvent(Key.CapsLock, KeyState.KeyUp))

        finalState shouldNotBeEqualComparingTo initialState
    }
}
