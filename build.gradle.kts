@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform") version "1.4.20"
}

group = "com.github.animeshz"
version = "0.0.1"

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    linuxX64()
    //    linuxArm64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                implementation("io.github.microutils:kotlin-logging:2.0.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.kotest:kotest-assertions-core:4.3.1")
                implementation("io.kotest:kotest-property:4.3.1")
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        //        val linuxArm64Main by getting {
        //            dependsOn(commonMain)
        //        }
        val mingwX64Main by getting {
            dependsOn(commonMain)
        }

        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}
