# High Level API

**Kotlin:** High Level API depends on [Keyboard][1] which is a wrapper around the [NativeKeyboardHandler][2].

**Java:** High Level API depends on [JKeyboard][4].

**NodeJS:** High Level API depends on [JsKeyboard][5].

## Adding a shortcut (Hotkey).

=== "Kotlin"
    ```kotlin
    keyboard.addShortcut(Key.LeftCtrl + Key.E, trigger = KeyState.KeyDown) {
        println("triggered")
    }
    ```
    <sup>**Note: The lambda is in suspend context, launched in context provided at time of instantiation of Keyboard (defaults to Dispatchers.Default).**</sup>

=== "NodeJS"
    ```js
    keyboard.addShortcut('LeftCtrl + E', true,
        () => console.log("triggered")
    );
    ```

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

<sup>**Note: `trigger` defaults to KeyState.KeyDown when not provided.**</sup><br>

## Send a [KeySet][3] to the host machine.

=== "Kotlin"
    ```kotlin
    keyboard.send(Key.LeftAlt + Key.M)
    ```

=== "NodeJS"
    ```js
    keyboard.send('LeftAlt + M');
    ```

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

## Write a sentence (String) on the host machine.

=== "Kotlin"
    ```kotlin
    keyboard.write("Hello Keyboard!")
    ```

=== "NodeJS"
    ```js
    keyboard.write('Hello Keyboard!');
    ```

=== "Java 8 or above"
    ```java
    keyboard.write("Hello Keyboard!");
    ```

## Wait till a [KeySet][3] is pressed.

Suspensive wait in Kotlin, whereas asynchronous `CompletableFuture<>` for Java

=== "Kotlin"
    ```kotlin
    keyboard.awaitTill(Key.LeftCtrl + Key.LeftShift + Key.R, trigger = KeyState.KeyDown)
    ```

=== "NodeJS"
    ```js
    await keyboard.completeWhenPressed('LeftCtrl + LeftShift + R');
    ```

=== "Java 8"
    ```java
    Set<Key> keys = new HashSet<>();
    Collections.addAll(keys, Key.LeftCtrl + Key.LeftShift + Key.R);

    keyboard.completeWhenPressed(new KeySet(keys), KeyState.KeyDown)
        .thenApply(unit -> {...});
    ```
    <sup>**Note: Unit is similar to java.lang.Void, a singleton object which has nothing to do for us.**</sup>

=== "Java 9 or above"
    ```java
    Set<Key> keys = Set.of(Key.LeftCtrl + Key.LeftShift + Key.R);

    keyboard.completeWhenPressed(new KeySet(keys), KeyState.KeyDown)
        .thenApply(unit -> {...});
    ```
    <sup>**Note: Unit is similar to java.lang.Void, a singleton object which has nothing to do for us.**</sup>


<sup>**Note: `trigger` defaults to KeyState.KeyDown when not provided.**</sup>

## Record Key presses till specific [KeySet][3].

Recorded KeyPresses is pushed into a [KeyPressSequence][1] (`List<Duration, KeyEvent>`)

=== "Kotlin"
    ```kotlin
    val records: KeyPressSequence = keyboard.recordTill(Key.LeftAlt + Key.A)
    ```

=== "NodeJS"
    ```js
    const records = await keyboard.recordKeyPressesTill('LeftCtrl + LeftShift + R');
    ```

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

## Play a recorded or created collection of Keys at defined order.

=== "Kotlin"
    ```kotlin
    keyboard.play(records, speedFactor = 1.25)
    ```

=== "NodeJS"
    ```js
    await keyboard.play(records, 1.0);
    ```

=== "Java 8 or above"
    ```java
    CompletableFuture<Unit> onFinish = keyboard.play(records, 1.25)
    ```

<sup>**Note: `speedFactor` defaults to 1.0 when not provided.**</sup>

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/KeySet.kt

[4]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/integration/keyboard-kt-jdk8/src/main/kotlin/com/github/animeshz/keyboard/JKeyboard.kt

[5]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard-kt/src/jsMain/kotlin/com/github/animeshz/keyboard/JsKeyboard.kt
