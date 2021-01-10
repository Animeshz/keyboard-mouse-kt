# High Level API

<style>
.tabbed-set {
    margin-top: 0 !important;
}
</style>

## Kotlin (Multiplatform / JVM)

High Level API depends on [Keyboard][4] which is a wrapper around the [NativeKeyboardHandler][1].

- Adding a shortcut (Hotkey).

    === "Kotlin"
        ```kotlin
        keyboard.addShortcut(Key.LeftCtrl + Key.E, trigger = KeyState.KeyDown) {
            println("triggered")
        }
        ```
    <sup>**Note: `trigger` defaults to KeyState.KeyDown when not provided.**</sup>

- Send a [KeySet][5] to the host machine.

    === "Kotlin"
        ```kotlin
        keyboard.send(Key.LeftAlt + Key.M)
        ```

- Write a sentence (String) on the host machine.

    === "Kotlin"
        ```kotlin
        keyboard.write("Hello Keyboard!")
        ```

- Suspensive wait till a [KeySet][5] is pressed.

    === "Kotlin"
        ```kotlin
        keyboard.awaitTill(Key.LeftCtrl + Key.LeftShift + Key.R, trigger = KeyState.KeyDown)
        ```
    <sup>**Note: `trigger` defaults to KeyState.KeyDown when not provided.**</sup>

- Record Key presses till specific [KeySet][5] is pressed into a [KeyPressSequence][6] (A list of pair of Duration and KeyEvent).

    === "Kotlin"
        ```kotlin
        val records: KeyPressSequence = keyboard.recordTill(Key.LeftAlt + Key.A)
        ```

- Play a recorded or created collection of Keys at defined order.

    === "Kotlin"
        ```kotlin
        keyboard.play(records, speedFactor = 1.25)
        ```
    <sup>**Note: `speedFactor` defaults to 1.0 when not provided.**</sup>

## Java (JVM)

High Level API depends on [JKeyboard][9].

- Adding a shortcut (Hotkey).

    === "Java 8"
        ```java
        Set<Key> keys = new HashSet<>();
        Collections.addAll(keys, Key.LeftCtrl, Key.E);

        keyboard.addShortcut(new KeySet(keys), KeyState.KeyDown,
            () -> System.out.println("triggered")
        );
        ```
    === "Java 9 or above"
        ```java
        Set<Key> keys = Set.of(Key.LeftCtrl, Key.E);

        keyboard.addShortcut(new KeySet(keys), KeyState.KeyDown,
            () -> System.out.println("triggered")
        );
        ```
    <sup>**Note: `trigger` defaults to KeyState.KeyDown when not provided.**</sup>

- Send a [KeySet][5] to the host machine.

    === "Java 8"
        ```java
        Set<Key> keys = new HashSet<>();
        Collections.addAll(keys, Key.LeftAlt, Key.M);

        keyboard.send(new KeySet(keys));
        ```
    === "Java 9 or above"
        ```java
        Set<Key> keys = Set.of(Key.LeftAlt, Key.M);

        keyboard.send(new KeySet(keys));
        ```

- Write a sentence (String) on the host machine.

    === "Java 8 or above"
        ```java
        keyboard.write("Hello Keyboard!");
        ```

- Asynchronous wait till a [KeySet][5] is pressed.

    === "Java 8"
        ```java
        Set<Key> keys = new HashSet<>();
        Collections.addAll(keys, Key.LeftCtrl + Key.LeftShift + Key.R);

        keyboard.completeWhenPressed(new KeySet(keys), KeyState.KeyDown)
            .thenApply(unit -> {...});
        ```
    === "Java 9 or above"
        ```java
        Set<Key> keys = Set.of(Key.LeftCtrl + Key.LeftShift + Key.R);

        keyboard.completeWhenPressed(new KeySet(keys), KeyState.KeyDown)
            .thenApply(unit -> {...});
        ```
    <sup>**Note: `trigger` defaults to KeyState.KeyDown when not provided.**</sup><br>
    <sup>**Note: Unit is similar to java.lang.Void, a singleton object which has nothing to do for us.**</sup>

- Record Key presses till specific [KeySet][5] is pressed into a list of pair of Duration and KeyEvent.

    === "Java 8"
        ```java
        Set<Key> keys = new HashSet<>();
        Collections.addAll(keys, Key.LeftAlt, Key.A);

        // `trigger` defaults to KeyState.KeyDown when not provided.
        CompletableFuture<List<Duration, KeyEvent>> records =
            keyboard.recordTill(new KeySet(keys));
        ```
    === "Java 9 or above"
        ```java
        Set<Key> keys = Set.of(Key.LeftAlt, Key.A);

        CompletableFuture<List<Duration, KeyEvent>> records =
            keyboard.recordTill(new KeySet(keys));
        ```

- Play a recorded or created collection of Keys at defined order at given speed.

    === "Java 8 or above"
        ```java
        CompletableFuture<Unit> onFinish = keyboard.play(records, 1.25)
        ```
    <sup>**Note: `speedFactor` defaults to 1.0 when not provided.**</sup>

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/Key.kt

[4]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt

[5]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/KeySet.kt

[6]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt#L33

[7]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/events/KeyEvent.kt
