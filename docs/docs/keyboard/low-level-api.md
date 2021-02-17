# Low Level API

**Kotlin:** Low Level API depends on [NativeKeyboardHandler][1] that can be obtained via `nativeKbHandlerForPlatform()`.

**Java:** Low Level API depends on [JNativeKeyboardHandler][4] that can be obtained via `JNativeKeyboardHandler.INSTANCE`.

**NodeJS:** Low Level API depends on [JsKeyboardHandler][5] that can be obtained via `JsNativeKeyboardHandler`.

## Importing the package.

=== "Kotlin (MPP)"
    ```kotlin
    import com.github.animeshz.keyboard.nativeKbHandlerForPlatform

    val handler = nativeKbHandlerForPlatform()
    ```

=== "NodeJS"
    ```js
    const kbkt = require('keyboard-kt');

    const handler = kbkt.com.github.animeshz.keyboard.JsNativeKeyboardHandler;
    ```
    <sup>**Note: This large import is due to limitations of K/JS to not able to export to global namespace currently, see [KT-37710](https://youtrack.jetbrains.com/issue/KT-37710).**</sup>

=== "Java 8 or above"
    ```java
    import com.github.animeshz.keyboard.JKeyboardHandler;

    JKeyboardHandler handler = JKeyboardHandler.INSTANCE;
    ```

## Listening to events using Flow (Kotlin) or callback (Java).

=== "Kotlin"
    ```kotlin
    handler.events
        .filter { it.state == KeyState.KeyDown }
        .map { it.key }
        .collect { println(it) }
    ```

=== "NodeJS"
    ```js
    handler.addHandler((key, pressed) => {
        if (pressed) {
            console.log(key);
        }
    });
    ```

=== "Java 8 or above"
    ```java
    handler.addHandler(keyEvent -> {
        if (keyEvent.state == KeyState.KeyDown) {
            System.out.println(keyEvent.key);
        }
    });
    ```

## Sending a [Key][2] event.
    
=== "Kotlin"
    ```kotlin
    handler.sendEvent(KeyEvent(Key.A, KeyState.KeyDown))
    ```

=== "NodeJS"
    ```js
    handler.sendEvent('A', true);
    ```

=== "Java 8 or above"
    ```java
    handler.sendEvent(new KeyEvent(Key.A, KeyState.KeyDown));
    ```


## Get [KeyState][3] (KeyDown or KeyUp) of the [Key](key.md).

=== "Kotlin"
    ```kotlin
    handler.getKeyState(Key.A)
    handler.getKeyState(Key.RightAlt)
    ```

=== "NodeJS"
    ```kotlin
    handler.getKeyState('A');
    handler.getKeyState('RightAlt');
    ```
    <sup>**Note: In JS it returns a boolean**</sup>

=== "Java 8 or above"
    ```js
    handler.getKeyState(Key.A);
    handler.getKeyState(Key.RightAlt);
    ```

## Get States of Toggleable Keys (returns a Boolean).

=== "Kotlin"
    ```kotlin
    handler.isCapsLockOn()
    handler.isNumLockOn()
    handler.isScrollLockOn()
    ```

=== "NodeJS"
    ```js
    handler.isCapsLockOn();
    handler.isNumLockOn();
    handler.isScrollLockOn();
    ```

=== "Java 8 or above"
    ```java
    handler.isCapsLockOn();
    handler.isNumLockOn();
    handler.isScrollLockOn();
    ```

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard-kt/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard-kt/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/Key.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard-kt/src/commonMain/kotlin/com/github/animeshz/keyboard/events/KeyEvent.kt

[4]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/integration/keyboard-kt-jdk8/src/main/kotlin/com/github/animeshz/keyboard/JNativeKeyboardHandler.kt

[5]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard-kt/src/jsMain/kotlin/com/github/animeshz/keyboard/JsKeyboardHandler.kt#L63
