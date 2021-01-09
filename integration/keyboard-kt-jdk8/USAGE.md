# Keyboard.kt JDK8 (For Java)

## USAGE

#### Low Level API

Low Level API depends on [JNativeKeyboardHandler][1] that can be obtained via [JNativeKeyboardHandler.INSTANCE][2].

- Listening to events using a callback.
  ```java
  handler.addHandler(keyEvent -> {
      if (keyEvent.state == KeyState.KeyDown) {
          System.out.println(keyEvent.key);
      }
  });
  ```
- Sending a [Key][3] event.
  ```java
  handler.sendEvent(new KeyEvent(Key.A, KeyState.KeyDown));
  ```
- Get [KeyState][7] (KeyDown or KeyUp) of the [Key][3].
  ```java
  handler.getKeyState(Key.A);
  handler.getKeyState(Key.RightAlt);
  ```
- Get States of Toggleable Keys (returns a Boolean).
  ```java
  handler.isCapsLockOn();
  handler.isNumLockOn();
  handler.isScrollLockOn();
  ```

#### High level API

High Level API depends on [JKeyboard][9].

- Adding a shortcut (Hotkey).
  ```java
  // Java 8
  Set<Key> keys = new HashSet<>();
  Collections.addAll(keys, Key.LeftCtrl, Key.E);
  // Java 9 or above
  Set<Key> keys = Set.of(Key.LeftCtrl, Key.E);

  // `trigger` defaults to KeyState.KeyDown when not provided.
  keyboard.addShortcut(new KeySet(keys), KeyState.KeyDown,
      () -> System.out.println("triggered"));
  ```
- Send a [KeySet][5] to the host machine.
  ```java
  keyboard.send(new KeySet(Set.of(Key.LeftAlt + Key.M)));
  ```
- Write a sentence (String) on the host machine.
  ```java
  keyboard.write("Hello Keyboard!");
  ```
- Asynchronous wait till a [KeySet][5] is pressed.
  ```java
  // `trigger` defaults to KeyState.KeyDown when not provided.
  CompletableFuture<Unit> future = keyboard.completeWhenPressed(new KeySet(Set.of(Key.LeftCtrl + Key.LeftShift + Key.R)), KeyState.KeyDown);
  future.thenApply(unit -> {...});  // Unit is similar to java.lang.Void a singleton object.
  ```
- Record Key presses till specific [KeySet][5] is pressed into a [KeyPressSequence][6].
  ```java
  CompletableFuture<List<Duration, KeyEvent>> records = keyboard.recordTill(new KeySet(Set.of(Key.LeftAlt + Key.A)));
  ```
- Play a recorded or created collection of Keys at defined order at given speed.
  ```java
  // `speedFactor` defaults to 1.0 when not provided.
  CompletableFuture<Unit> onFinish = keyboard.play(records, 1.25)
  ```

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/NativeKeyboardHandler.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/NativeKeyboardHandler.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/entity/Key.kt

[4]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/Keyboard.kt

[5]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/entity/KeySet.kt

[6]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/Keyboard.kt#L33

[7]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard-kt/events/KeyEvent.kt
