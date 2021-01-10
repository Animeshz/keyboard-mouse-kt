# Keyboard.kt

## USAGE

### Low Level API

Low Level API depends on [NativeKeyboardHandler][1] that can be obtained via [nativeKbHandlerForPlatform][2].

- Listening to events using Flow.
  ```kotlin
  handler.events
      .filter { it.state == KeyState.KeyDown }
      .map { it.key }
      .collect { println(it) }
  ```
- Sending a [Key][3] event.
  ```kotlin
  handler.sendEvent(KeyEvent(Key.A, KeyState.KeyDown))
  ```
- Get [KeyState][7] (KeyDown or KeyUp) of the [Key][3].
  ```kotlin
  handler.getKeyState(Key.A)
  handler.getKeyState(Key.RightAlt)
  ```
- Get States of Toggleable Keys (returns a Boolean).
  ```kotlin
  handler.isCapsLockOn()
  handler.isNumLockOn()
  handler.isScrollLockOn()
  ```

### High level API

High Level API depends on [Keyboard][4] which is a wrapper around the [NativeKeyboardHandler][1].

- Adding a shortcut (Hotkey).
  ```kotlin
  // `trigger` when not provided defaults to KeyState.KeyDown
  keyboard.addShortcut(Key.LeftCtrl + Key.E, trigger = KeyState.KeyDown) {
      println("triggered")
  }
  ```
- Send a [KeySet][5] to the host machine.
  ```kotlin
  keyboard.send(Key.LeftAlt + Key.M)
  ```
- Write a sentence (String) on the host machine.
  ```kotlin
  keyboard.write("Hello Keyboard!")
  ```
- Suspensive wait till a [KeySet][5] is pressed.
  ```kotlin
  // `trigger` when not provided defaults to KeyState.KeyDown
  keyboard.awaitTill(Key.LeftCtrl + Key.LeftShift + Key.R)
  ```
- Record Key presses till specific [KeySet][5] is pressed into a [KeyPressSequence][6].
  ```kotlin
  val records: KeyPressSequence = keyboard.recordTill(Key.LeftAlt + Key.A)
  ```
- Play a recorded or created collection of Keys at defined order.
  ```kotlin
  keyboard.play(records, speedFactor = 1.25)
  ```

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/NativeKeyboardHandler.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/NativeKeyboardHandler.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/entity/Key.kt

[4]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/Keyboard.kt

[5]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/entity/KeySet.kt

[6]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/Keyboard.kt#L33

[7]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/events/KeyEvent.kt
