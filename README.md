# KeyboardMouse.kt

<p>
    <a href="https://github.com/Animeshz/keyboard-mouse-kt/releases">
        <img src="https://img.shields.io/github/release-date/Animeshz/keyboard-mouse-kt?style=flat-square&label=Latest%20Release" alt="Latest Release" />
    </a>
    <a href="https://bintray.com/animeshz/maven/keyboard-kt">
        <img src="https://img.shields.io/bintray/v/animeshz/maven/keyboard-kt?color=blue&style=flat-square" alt="Bintray Version">
    </a>
    <img src="https://img.shields.io/github/languages/code-size/Animeshz/keyboard-mouse-kt?style=flat-square" alt="Code Size"/>
    <a href="https://github.com/Animeshz/keyboard-mouse-kt/blob/master/LICENSE">
        <img src="https://img.shields.io/github/license/Animeshz/keyboard-mouse-kt?style=flat-square" alt="License" />
    </a>
</p>

A lightweight multiplatform kotlin library for interacting with global keyboard and mouse events.

__KeyboardMouse.kt is still in an experimental stage, as such we can't guarantee API stability between releases. While
we'd love for you to try out our library, we don't recommend you use this in production just yet.__

## What is KeyboardMouse.kt

KeyboardMouse.kt is a lightweight, coroutine-based multiplatform kotlin library for idiomatically interacting with Keyboard and
Mouse (receiving and sending global events).

We aim to provide high-level as well as high-performant low-level access to such APIs. See the usage section below to
know more!

## Status of KeyboardMouse.kt

- [ ] Keyboard
    - [X] Windows
        - [X] x86_64 (64 bit)
        - [ ] x86    (32 bit)
    - [X] Linux
        - [X] x86_64 (64 bit)
        - [ ] x86    (32 bit)
    - [ ] MacOS
    - [ ] JVM
        - [X] Windows x86_64 (64 bit)
        - [X] Windows x86    (32 bit)
        - [X] Linux x86_64 (64 bit)
        - [X] Linux x86    (32 bit)
        - [ ] Linux Arm32
        - [ ] Linux Arm64
- [ ] Mouse
    - [ ] Windows
    - [ ] Linux
    - [ ] MacOS
    - [ ] JVM

## Installation

To add the library to your project, add the following the repository and dependency (`build.gradle.kts`):

```kotlin
repositories {
    maven(url = "https://dl.bintray.com/animeshz/maven")
}

kotlin {
    // Your targets
    jvm()
    mingwX64 {
        binaries { executable { entryPoint = "main" } }
    }
    linuxX64 {
        binaries { executable { entryPoint = "main" } }
    }

    // Dependency to the library
    sourceSets {
        // Either as common
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("com.github.animeshz:keyboard-kt:<version>")
                implementation("com.github.animeshz:mouse-kt:<version>")
            }
        }
        
        // Or configure each-platform by the suffix
        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("com.github.animeshz:keyboard-kt-jvm:<version>")
                implementation("com.github.animeshz:mouse-kt-jvm:<version>")
            }
        }
    }
}
```

## Usage

### Low Level API:

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

### High level API:

High Level API depends on [Keyboard][4] which is a wrapper around the [NativeKeyboardHandler][1].

- Adding a shortcut (Hotkey).
  ```kotlin
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

A few more examples can be found at [keyboard/src/commonTest/examples](https://github.com/Animeshz/keyboard-mouse-kt/tree/master/keyboard/src/commonTest/kotlin/examples)

## Contributing and future plans

The Github dicussions are open! Be sure to show your existence, say hi! and share if you have any upcoming ideas :)

Issues and PRs are always welcome!

For future plans and contributing to the project please checkout [CONTRIBUTING.md](https://github.com/Animeshz/keyboard-mouse-kt/blob/master/CONTRIBUTING.md)


[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/Key.kt

[4]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt

[5]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/KeySet.kt

[6]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt#L33

[7]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/events/KeyEvent.kt
