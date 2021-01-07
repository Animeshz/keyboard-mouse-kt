package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import io.kotest.matchers.comparables.shouldNotBeEqualComparingTo
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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

    @Test
    fun `Test send and receive event`() = runBlockingTest {
        val handler = nativeKbHandlerForPlatform()

        launch {
            handler.sendEvent(KeyEvent(Key.LeftCtrl, KeyState.KeyDown))
            handler.sendEvent(KeyEvent(Key.LeftCtrl, KeyState.KeyUp))
        }

        val events = withTimeout(100) { handler.events.dropWhile { it.key != Key.LeftCtrl }.take(2).toList() }

        events[0] should {
            it.key shouldBe Key.LeftCtrl
            it.state shouldBe KeyState.KeyDown
        }
        events[1] should {
            it.key shouldBe Key.LeftCtrl
            it.state shouldBe KeyState.KeyUp
        }
    }
}
