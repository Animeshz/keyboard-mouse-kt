# High Level API

High Level API depends on [Keyboard][1] which is a wrapper around the [NativeKeyboard][2].

## Importing the package.

=== "Kotlin"
    ```kotlin
    import io.github.animeshz.keyboard.Keyboard

    val keyboard = Keyboard
    ```

=== "Java 8 or above"
    ```java
    import io.github.animeshz.keyboard.Keyboard;

    Keyboard keyboard = Keyboard.INSTANCE;
    ```

=== "NodeJS"
    ```js
    const kt = require('keyboard-mouse-kt');

    const keyboard = kbkt.io.github.animeshz.keyboard.Keyboard;
    ```
    <sup>**Note: This large import is due to limitations of K/JS to not able to export to global namespace currently, see [KT-37710](https://youtrack.jetbrains.com/issue/KT-37710).**</sup>

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
    keyboard.addShortcut(parseKeySet('LeftCtrl + E'), onPressed(true),
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
    keyboard.send(parseKeySet('LeftAlt + M'));
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

## Trigger one time once when a [KeySet][3] is pressed.

=== "Kotlin"
    ```kotlin
    keyboard.triggerWhen(Key.LeftCtrl + Key.LeftShift + Key.R, trigger = KeyState.KeyDown) {
        // ...
    }
    ```

=== "NodeJS"
    ```js
    keyboard.triggerWhen(parseKeySet('LeftCtrl + LeftShift + R'), onPressed(true), () => {
        // ...
    });
    ```

=== "Java 8"
    ```java
    Set<Key> keys = new HashSet<>();
    Collections.addAll(keys, Key.LeftCtrl + Key.LeftShift + Key.R);

    keyboard.triggerWhen(new KeySet(keys), KeyState.KeyDown, () -> {
        // ...
    });
    ```

=== "Java 9 or above"
    ```java
    Set<Key> keys = Set.of(Key.LeftCtrl + Key.LeftShift + Key.R);

    keyboard.triggerWhen(new KeySet(keys), KeyState.KeyDown, () -> {
        // ...
    });
    ```

<sup>**Note: `trigger` defaults to KeyState.KeyDown when not provided.**</sup>

## Record Key presses till specific [KeySet][3].

Recorded KeyPresses is pushed into a [KeyPressSequence][1] (`Array<Triple<Duration, Key, KeyState>>`)

=== "Kotlin"
    ```kotlin
    val records: KeyPressSequence = keyboard.recordTill(Key.LeftAlt + Key.A, trigger = KeyState.KeyDown) { records ->
        // ...
    }
    ```

=== "NodeJS"
    ```js
    keyboard.recordKeyPressesTill('LeftCtrl + LeftShift + R', true. (records) => {

    });
    ```

=== "Java 8"
    ```java
    Set<Key> keys = new HashSet<>();
    Collections.addAll(keys, Key.LeftAlt, Key.A);

    // `trigger` defaults to KeyState.KeyDown when not provided.
    keyboard.recordTill(new KeySet(keys), KeyState.KeyDown, (records) -> {
        // ...
    });
    ```

=== "Java 9 or above"
    ```java
    Set<Key> keys = Set.of(Key.LeftAlt, Key.A);

    CompletableFuture<List<Duration, KeyEvent>> records =
        keyboard.recordTill(new KeySet(keys), KeyState.KeyDown);
    ```

<sup>**Note: Trigger here is to stop recording.**</sup>

## Play a recorded or created collection of Keys at defined order.

Blocks in JVM and Native targets, whereas enqueues the task to the event loop using `setTimeout` in JS.

=== "Kotlin"
    ```kotlin
    keyboard.play(records, speedFactor = 1.25)
    ```

=== "NodeJS"
    ```js
    keyboard.play(records, 1.25);
    ```

=== "Java 8 or above"
    ```java
    CompletableFuture<Unit> onFinish = keyboard.play(records, 1.25)
    ```

<sup>**Note: `speedFactor` defaults to 1.0 when not provided.**</sup>

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/io/github/animeshz/keyboard/Keyboard.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/io/github/animeshz/keyboard/NativeKeyboard.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/io/github/animeshz/keyboard/entity/KeySet.kt
