@file:Suppress("UNUSED_VARIABLE")

import com.github.animeshz.keyboard_mouse.configuration.Target
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("multiplatform")
    id("keyboard-mouse-configuration")
    id("keyboard-mouse-publishing")
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

kotlin {
    jvm()
    js {
        useCommonJs()
        nodejs()
    }
    linuxX64 {
        val main by compilations.getting

        main.cinterops.create("device") { defFile("src/linuxX64Main/cinterop/device.def") }
        main.cinterops.create("x11") { defFile("src/linuxX64Main/cinterop/x11.def") }
    }
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2-native-mt")
                implementation("org.jetbrains.kotlinx:atomicfu:0.14.4")
            }
        }
        val commonTest by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.mockk:mockk-common:1.10.3")
                implementation("io.kotest:kotest-assertions-core:4.4.0.RC2")
            }
        }

        val jvmMain by sourceSets.getting { dependsOn(commonMain) }
        val jvmTest by sourceSets.getting {
            dependsOn(commonTest)
            dependsOn(jvmMain)
            dependencies {
                implementation(kotlin("test-junit5"))
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
                implementation("io.kotest:kotest-assertions-core:4.3.2")
            }
        }

        val jsMain by sourceSets.getting {
            dependencies {
                implementation(devNpm("node-addon-api", "*"))
            }
        }
        val jsTest by sourceSets.getting {
            dependsOn(commonTest)
            dependsOn(jsMain)
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val linuxX64Main by sourceSets.getting { dependsOn(commonMain) }
        val linuxX64Test by sourceSets.getting {
            dependsOn(commonTest)
            dependsOn(linuxX64Main)
        }

        val mingwX64Main by sourceSets.getting { dependsOn(commonMain) }
        val mingwX64Test by sourceSets.getting {
            dependsOn(commonTest)
            dependsOn(mingwX64Main)
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    explicitApi()
}

publishingConfig {
    repository = "https://api.bintray.com/maven/animeshz/maven/keyboard-kt/;publish=1;override=1"
    username = System.getenv("BINTRAY_USER")
    password = System.getenv("BINTRAY_KEY")
}

configureJni {
    headers {
        inputDir = "src/jvmMain/kotlin"
        outputDir = "src/jvmMain/generated/jni"
    }
    compilation {
        baseInputPaths = listOf("src/jvmMain/jni", "src/nativeCommon")
        outputDir = "build/jni"

        targets = listOf(
            Target("windows", "x64", "animeshz/keyboard-mouse-kt:jni-build-windows-x64"),
            Target("windows", "x86", "animeshz/keyboard-mouse-kt:jni-build-windows-x86"),
            Target("linux", "x64", "animeshz/keyboard-mouse-kt:jni-build-linux-x64"),
            Target("linux", "x86", "animeshz/keyboard-mouse-kt:jni-build-linux-x86")
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
