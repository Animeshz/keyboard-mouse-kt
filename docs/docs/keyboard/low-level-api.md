# Low Level API

<style>
.tabbed-set {
    margin-top: 0 !important;
}
</style>

## Kotlin (Multiplatform / JVM)

Low Level API depends on [NativeKeyboardHandler][1] that can be obtained via [nativeKbHandlerForPlatform][2].

- Listening to events using Flow.

    === "Kotlin"
        ```kotlin
        handler.events
            .filter { it.state == KeyState.KeyDown }
            .map { it.key }
            .collect { println(it) }
        ```

- Sending a [Key][3] event.
    
    === "Kotlin"
        ```kotlin
        handler.sendEvent(KeyEvent(Key.A, KeyState.KeyDown))
        ```

- Get [KeyState][7] (KeyDown or KeyUp) of the [Key][3].

    === "Kotlin"
        ```kotlin
        handler.getKeyState(Key.A)
        handler.getKeyState(Key.RightAlt)
        ```

- Get States of Toggleable Keys (returns a Boolean).

    === "Kotlin"
        ```kotlin
        handler.isCapsLockOn()
        handler.isNumLockOn()
        handler.isScrollLockOn()
        ```

## Java (JVM)

Low Level API depends on [JNativeKeyboardHandler][1] that can be obtained via [JNativeKeyboardHandler.INSTANCE][2].

- Listening to events using a callback.

    === "Java 8 or above"
        ```java
        handler.addHandler(keyEvent -> {
            if (keyEvent.state == KeyState.KeyDown) {
                System.out.println(keyEvent.key);
            }
        });
        ```
- Sending a [Key][3] event.

    === "Java 8 or above"
        ```java
        handler.sendEvent(new KeyEvent(Key.A, KeyState.KeyDown));
        ```
- Get [KeyState][7] (KeyDown or KeyUp) of the [Key][3].

    === "Java 8 or above"
        ```java
        handler.getKeyState(Key.A);
        handler.getKeyState(Key.RightAlt);
        ```
- Get States of Toggleable Keys (returns a Boolean).

    === "Java 8 or above"
        ```java
        handler.isCapsLockOn();
        handler.isNumLockOn();
        handler.isScrollLockOn();
        ```

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/Key.kt

[4]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt

[5]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/KeySet.kt

[6]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt#L33

[7]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/events/KeyEvent.kt