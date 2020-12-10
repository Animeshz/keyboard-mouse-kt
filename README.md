# KeyboardMouse.kt


A multiplatform kotlin library for listening to global keyboard and mouse events.

__KeyboardMouse.kt is still in an experimental stage, as such we can't guarantee API stability between releases. While we'd love for you to try out our library, we don't recommend you use this in production just yet.__

## What is KeyboardMouse.kt

KeyboardMouse.kt is a coroutine-based cross-platform implementation of Global Keyboard and Mouse events, written 100% in Kotlin.

We aim to provide high-level as well as high-performant low-level access to such APIs. Sometimes you have to do some unconventional things, and we want to allow you to do those in concise, safe and supported way.

## Status of KeyboardMouse.kt

  - [ ] Keyboard
    - [X] Windows <sup>1</sup>
    - [X] Linux <sup>1</sup>
    - [ ] MacOS
    - [ ] JVM
  - [ ] Mouse
    - [ ] Windows
    - [ ] Linux
    - [ ] MacOS
    - [ ] JVM

<sub>1. Almost done, few optimizations and tests are remaining (tests are on hold due to MockK does not support K/N).</sub>


## Installation

At this early stage, library is not published to a any public repository.

You can currently clone the project and publish it to your mavenLocal repository on your host machine and use it.

1. Clone the repo:
   `$ git clone https://github.com/Animeshz/keyboard-mouse-kt.git`

2. Build and publish to mavenLocal:
   `$ ./gradlew build publishToMavenLocal`

   <sub>Note: When building for linux, you need libX11 and XInput2 (for Ubuntu: `apt install libX11-dev liblxi-dev`, for
   Arch: `pacman -S libx11 libxi`)</sub>

3. Add the library to your project (`build.gradle.kts`):

  ```kotlin
  plugins {
      kotlin("multiplatform") version "1.4.10"
  }

  repositories {
      mavenLocal()
  }

  kotlin {
      // Your targets
      mingwX64 {
          binaries { executable { entryPoint = "main" } }
      }
      linuxX64 {
          binaries { executable { entryPoint = "main" } }
      }

      sourceSets {
          // Either in common:
          val commonMain by getting {
              dependencies {
                  implementation(kotlin("stdlib-common"))
                  implementation("com.github.animeshz:keyboard:0.0.1")
                  implementation("com.github.animeshz:mouse:0.0.1")
              }
          }

          // Or configuring per platform:
          val mingwX64Main by getting {
              dependencies {
                  implementation("com.github.animeshz:keyboard-mingwx64:0.0.1")
                  implementation("com.github.animeshz:mouse-mingwx64:0.0.1")
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

[1]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[2]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/NativeKeyboardHandler.kt

[3]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/Key.kt

[4]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt

[5]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/entity/KeySet.kt

[6]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/Keyboard.kt#L31

[7]: https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonMain/kotlin/com/github/animeshz/keyboard/events/KeyEvent.kt
