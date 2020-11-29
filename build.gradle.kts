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
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
//                implementation("io.kotest:kotest-assertions-core:4.3.1")
//                implementation("io.kotest:kotest-property:4.3.1")
            }
        }
    }
    linuxX64()
    linuxArm64()
    mingwX64()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
