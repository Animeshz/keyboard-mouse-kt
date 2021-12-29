# Low Level API

Low Level API depends on [NativeKeyboard][1] that can be obtained via `NativeKeyboard` in Kotlin and `NativeKeyboard.INSTANCE` in JS and Java.

## Importing the package.

=== "Kotlin"
    ```kotlin
    import io.github.animeshz.kbms.keyboard.NativeKeyboard

    val native = NativeKeyboard
    ```

=== "NodeJS"
    ```js
    const kt = require('keyboard-mouse-kt');

    const native = kt.io.github.animeshz.kbms.keyboard.NativeKeyboard;
    ```
    <sup>**Note: This large import is due to limitations of K/JS to not able to export to global namespace currently, see [KT-37710](https://youtrack.jetbrains.com/issue/KT-37710).**</sup>


=== "Java 8 or above"
    ```java
    import io.github.animeshz.kbms.keyboard.NativeKeyboard;

    NativeKeyboard native = NativeKeyboard.INSTANCE;
    ```

## Listening to events using Flow (Kotlin) or callback (Java).

=== "Kotlin"
    ```kotlin
    val id = native.addEventHandler { id, keyCode, isPressed ->
        if (isPressed) {
            println(Key.fromKeyCode(keyCode))
        }

        if (keyCode == Key.J.keyCode) native.removeEventHandler(id)
    }

    // or here
    native.removeEventHandler(id)
    ```

=== "Kotlin (with coroutines)"
    ```kotlin
    native.events
        .filter { it.isPressed }
        .map { it.key }
        .takeWhile { it.key == Key.J }
        .collect { println(it) }
    ```

=== "NodeJS"
    ```js
    let id = native.addHandler((id, keyCode, isPressed) => {
        if (isPressed) {
            console.log(key);
        }

        if (keyCode == parseKey("J").keyCode) native.removeEventHandler(id);
        // or
        if (parseKeyCode(keyCode) == parseKey("J")) native.removeEventHandler(id);
    });
    ```

=== "Java 8 or above"
    ```java
    int id = native.addEventHandler((id, keyCode, isPressed) -> {
        if (isPressed) {
            System.out.println(Key.fromKeyCode(keyCode));
        }

        if (keyCode == Key.J.keyCode) native.removeEventHandler(id);
    });
    ```

## Sending a [Key][2] event.
    
=== "Kotlin"
    ```kotlin
    native.sendEvent(Key.A.keyCode, isPressed = true)
    ```

=== "NodeJS"
    ```js
    native.sendEvent(parseKey('A').keyCode, true);
    ```

=== "Java 8 or above"
    ```java
    native.sendEvent(Key.A.keyCode, true);
    ```


## Check if the [Key][2] is pressed or not.

=== "Kotlin"
    ```kotlin
    native.isPressed(Key.A.keyCode)
    native.isPressed(Key.RightAlt.keyCode)
    ```

=== "NodeJS"
    ```kotlin
    native.isPressed(parseKey('A').keyCode);
    native.isPressed(parseKey('RightAlt').keyCode);
    ```
    <sup>**Note: In JS it returns a boolean**</sup>

=== "Java 8 or above"
    ```js
    native.isPressed(Key.A.keyCode);
    native.isPressed(Key.RightAlt.keyCode);
    ```

## Get States of Toggleable Keys (returns a Boolean).

=== "Kotlin"
    ```kotlin
    native.isCapsLockOn()
    native.isNumLockOn()
    native.isScrollLockOn()
    ```

=== "NodeJS"
    ```js
    native.isCapsLockOn();
    native.isNumLockOn();
    native.isScrollLockOn();
    ```

=== "Java 8 or above"
    ```java
    native.isCapsLockOn();
    native.isNumLockOn();
    native.isScrollLockOn();
    ```

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard-mouse-kt/src/commonMain/kotlin/io/github/animeshz/keyboard/NativeKeyboard.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard-mouse-kt/src/commonMain/kotlin/io/github/animeshz/keyboard/entity/Key.kt
