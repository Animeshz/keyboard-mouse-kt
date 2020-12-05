KeyboardMouse.kt
=======

A multiplatform library for listening to global keyboard and mouse events.

__KeyboardMouse.kt is still in an experimental stage, as such we can't guarantee API stability between releases. While we'd love for you to try out our library, we don't recommend you use this in production just yet.__

What is KeyboardMouse.kt
========================

KeyboardMouse.kt is a coroutine-based cross-platform implementation of Global Keyboard and Mouse events, written 100% in Kotlin.

We aim to provide high-performant low-level as well as high-level access to such APIs
 
Low level APIs include ([NativeKeyboardHandler]()):
  - Listening to events using Flow
  - Sending a single key event
    
High level APIs include ([Keyboard]()):
  - Adding a shortcut (Hotkey).
  - Press a [KeySet]().

Status of KeyboardMouse.kt
==========================

  - [ ] Keyboard
    - [ ] Windows <sup>1</sup>
    - [ ] Linux <sup>1</sup>
    - [ ] MacOS
    - [ ] JVM
  - [ ] Mouse
    - [ ] Windows
    - [ ] Linux
    - [ ] MacOS
    - [ ] JVM

<sub>1. Almost done, few optimizations and tests are remaining (tests are on hold due to MockK does not support K/N).</sub>


Installation
============

At this early stage, library is not published to a any public repository.

You can currently clone the project and publish it to your mavenLocal repository on your host machine and use it.

  1. Clone the repo:
    `$ git clone https://github.com/Animeshz/keyboard-mouse-kt.git`
   
  2. Build and publish to mavenLocal:
    `gradle cinteropCinteropLinuxX64 publishToMavenLocal`
  
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
        mingwX64() {
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
