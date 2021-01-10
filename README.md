# KeyboardMouse.kt

<p>
    <a href="https://github.com/Animeshz/keyboard-mouse-kt/releases">
        <img src="https://img.shields.io/github/release-date/Animeshz/keyboard-mouse-kt?style=flat-square&label=Latest%20Release" alt="Latest Release" />
    </a>
    <a href="https://bintray.com/animeshz/maven/keyboard-mouse-kt">
        <img src="https://img.shields.io/bintray/v/animeshz/maven/keyboard-mouse-kt?color=blue&style=flat-square" alt="Bintray Version">
    </a>
    <img src="https://img.shields.io/github/languages/code-size/Animeshz/keyboard-mouse-kt?style=flat-square" alt="Code Size"/>
    <a href="https://github.com/Animeshz/keyboard-mouse-kt/blob/master/LICENSE">
        <img src="https://img.shields.io/github/license/Animeshz/keyboard-mouse-kt?style=flat-square" alt="License" />
    </a>
</p>

__KeyboardMouse.kt is still in an experimental stage, as such we can't guarantee API stability between releases. While
we'd love for you to try out our library, we don't recommend you use this in production just yet.__

## What is KeyboardMouse.kt

KeyboardMouse.kt is a lightweight, coroutine-based multiplatform kotlin library for idiomatically interacting with Keyboard and
Mouse (receiving and sending global events).

We aim to provide high-level as well as high-performant low-level access to such APIs. See the usage section below to
know more!



## Installation

To add the library to your project, add the following the repository and dependency (`build.gradle.kts`):

### Gradle (Groovy)

```groovy
repositories {
    maven { url "https://dl.bintray.com/animeshz/maven" }
}
```
```groovy
dependencies {
    // In commonMain (targeting Kotlin/Multiplatform)
    implementation("com.github.animeshz:keyboard-kt:<version>")
    implementation("com.github.animeshz:mouse-kt:<version>")

    // In platformMain (targeting particular platform in Kotlin), e.g. -jvm, -linuxX64, etc.
    implementation("com.github.animeshz:keyboard-kt-<platform>:<version>")
    implementation("com.github.animeshz:mouse-kt<platform>:<version>")

    // Using from Java 8 (with complete Java support)
    implementation("com.github.animeshz:keyboard-kt-jdk8:<version>")
}
```

### Gradle (Kotlin)

```kotlin
repositories {
    maven(url = "https://dl.bintray.com/animeshz/maven")
}
```
```kotlin
dependencies {
    // In commonMain (targeting Kotlin/Multiplatform)
    implementation("com.github.animeshz:keyboard-kt:<version>")
    implementation("com.github.animeshz:mouse-kt:<version>")

    // In platformMain (targeting particular platform in Kotlin), e.g. -jvm, -linuxX64, etc.
    implementation("com.github.animeshz:keyboard-kt-<platform>:<version>")
    implementation("com.github.animeshz:mouse-kt<platform>:<version>")

    // Using from Java 8 (with complete Java support)
    implementation("com.github.animeshz:keyboard-kt-jdk8:<version>")
}
```

<details>
    <summary><b>A sample `bulid.gradle.kts` script when targeting Kotlin/Multiplatform.</b></summary>

As we all know the dependencies must be specified in their particular scopes in when targeting Kotlin/Multiplatform.

```
plugins {
    kotlin("mutliplatform") version "<kotlin-version>"
}

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
</details>

## Usage

### Keyboard

#### From Kotlin (Multiplatform)

<b>See: [keyboard-kt/USAGE.md](https://github.com/Animeshz/keyboard-mouse-kt/tree/master/keyboard-kt/USAGE.md)</b>

#### From Java (JVM)

<b>See: [integration/keyboard-kt-jdk8/USAGE.md](https://github.com/Animeshz/keyboard-mouse-kt/tree/master/integration/keyboard-kt-jdk8/USAGE.md)</b>

A few more examples can be found at [keyboard/src/commonTest/examples](https://github.com/Animeshz/keyboard-mouse-kt/tree/master/keyboard-kt/src/commonTest/kotlin/examples)

## Contributing and future plans

The Github dicussions are open! Be sure to show your existence, say hi! and share if you have any upcoming ideas :)

Issues and PRs are always welcome!

For future plans and contributing to the project please checkout [CONTRIBUTING.md](https://github.com/Animeshz/keyboard-mouse-kt/blob/master/CONTRIBUTING.md)
