@file:Suppress("UNUSED_VARIABLE")

import io.github.animeshz.keyboard_mouse.native_compile.Target
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("multiplatform")
    id("keyboard-mouse-native-compile")
    id("keyboard-mouse-publishing")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        moduleName = "keyboard-mouse-kt"

        useCommonJs()
        nodejs()
        binaries.library()
    }

    val linuxSettings: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.() -> Unit = {
        val main by compilations.getting

        main.cinterops.create("native") {
            defFile("src/nativeMain/cinterop/native.def")
            extraOpts("-Xsource-compiler-option", "-Isrc/nativeCommon")
            extraOpts("-Xsource-compiler-option", "-Isrc/nativeCommon/linux")
            extraOpts("-Xsource-compiler-option", "-Isrc/nativeMain/cinterop")
            extraOpts("-Xcompile-source", "src/nativeMain/cinterop/exports.cpp")
        }
    }

    val mingwSettings: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.() -> Unit = {
        val main by compilations.getting

        main.cinterops.create("native") {
            defFile("src/nativeMain/cinterop/native.def")
            extraOpts("-Xsource-compiler-option", "-Isrc/nativeCommon")
            extraOpts("-Xsource-compiler-option", "-Isrc/nativeCommon/windows")
            extraOpts("-Xsource-compiler-option", "-Isrc/nativeMain/cinterop")
            extraOpts("-Xcompile-source", "src/nativeMain/cinterop/exports.cpp")
        }
    }

    // https://github.com/touchlab/Stately/issues/62
    linuxX64(configure = linuxSettings)
    // linuxArm64(configure = linuxSettings)
    linuxArm32Hfp(configure = linuxSettings)

    mingwX64(configure = mingwSettings)
    // https://github.com/touchlab/Stately/issues/62
    // mingwX86()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by sourceSets.getting { dependsOn(commonMain) }
        val jvmTest by sourceSets.getting { dependsOn(commonTest) }

        val jsMain by sourceSets.getting { dependsOn(commonMain) }
        val jsTest by sourceSets.getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

//        val linuxMain by sourceSets.creating { dependsOn(commonMain) }
//        val linuxX64Main by sourceSets.getting { dependsOn(linuxMain) }
//        // val linuxArm64Main by sourceSets.getting { dependsOn(linuxMain) }
//        val linuxArm32HfpMain by sourceSets.getting { dependsOn(linuxMain) }
//
//        val mingwMain by sourceSets.creating { dependsOn(commonMain) }
//        val mingwX64Main by sourceSets.getting { dependsOn(mingwMain) }
//        // val mingwX86Main by sourceSets.getting { dependsOn(mingwMain) }

        val nativeMain by sourceSets.creating { dependsOn(commonMain) }
        val linuxX64Main by sourceSets.getting { dependsOn(nativeMain) }
        val linuxArm32HfpMain by sourceSets.getting { dependsOn(nativeMain) }
        val mingwX64Main by sourceSets.getting { dependsOn(nativeMain) }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    explicitApi()
}

publishingConfig {
    repository = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
    username = System.getenv("SONATYPE_USER")
    password = System.getenv("SONATYPE_KEY")
}

nativeCompilation {
    dockerImage = "animeshz/keyboard-mouse-kt:latest"

    jni {
        headers {
            inputDir = "src/jvmMain/kotlin"
            outputDir = "src/jvmMain/generated/jni"
        }
        compilation {
            baseInputPaths = listOf("src/jvmMain/cpp", "src/nativeCommon")
            outputDir = "build/jni"
            targets = listOf(
                Target("windows", "x64", "source windows64"),
                Target("windows", "x86", "source windows32"),
                Target("linux", "x64", "source linux64"),
                Target("linux", "x86", "source linux32")
            )
        }
    }

    napi {
        baseInputPaths = listOf("src/jsMain/cpp", "src/nativeCommon")
        outputDir = "build/napi"

        targets = listOf(
            Target("windows", "x64", "source windows64"),
            Target("windows", "x86", "source windows32"),
            Target("linux", "x64", "source linux64")
            // NodeJS doesn't ship in x86, so people must be building the nodejs their selves, so supporting it is not really necessary for now
            // Target("linux", "x86", "source linux32")
        )
    }
}

tasks.withType<AbstractTestTask> {
    testLogging {
        showStackTraces = true
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.withType<KotlinJsCompile> {
    kotlinOptions.sourceMap = false
}

tasks.getByName<Test>("jvmTest") {
    useJUnitPlatform()
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    filter {
        exclude("**/cinterop/**")
    }
}
