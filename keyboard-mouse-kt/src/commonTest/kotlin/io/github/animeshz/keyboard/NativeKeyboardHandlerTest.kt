package io.github.animeshz.keyboard

import io.github.animeshz.keyboard.entity.Key
import io.kotest.matchers.comparables.shouldNotBeEqualComparingTo
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsName
import kotlin.test.Test

@ExperimentalJsExport
@ExperimentalKeyIO
class NativeKeyboardHandlerTest {
    @Test
    @JsName("Caps_lock_key_should_be_toggled_when_KeyDown_event_is_triggered")
    fun `Caps lock key should be toggled when KeyDown event is triggered`() {
        val native = NativeKeyboard

        val initialState = native.isCapsLockOn()

        native.sendEvent(Key.CapsLock.keyCode, true)
        native.sendEvent(Key.CapsLock.keyCode, false)

        val finalState = native.isCapsLockOn()

        // Set the state back to initialState
        native.sendEvent(Key.CapsLock.keyCode, true)
        native.sendEvent(Key.CapsLock.keyCode, false)

        finalState shouldNotBeEqualComparingTo initialState
    }

    @Test
    @JsName("Test_send_and_receive_event")
    fun `Test send and receive event`() {
        val native = NativeKeyboard

        val events = ArrayList<Pair<Int, Boolean>>(2)
        native.addEventHandler { id, keyCode, isPressed ->
            if (events.size == 2) return@addEventHandler native.removeEventHandler(id)
            if(keyCode != Key.LeftCtrl.keyCode) return@addEventHandler

            events.add(keyCode to isPressed)
        }

        native.sendEvent(Key.LeftCtrl.keyCode, true)
        native.sendEvent(Key.LeftCtrl.keyCode, false)

        events[0] should {
            it.first shouldBe Key.LeftCtrl.keyCode
            it.second shouldBe true
        }
        events[1] should {
            it.first shouldBe Key.LeftCtrl.keyCode
            it.second shouldBe false
        }
    }
}
