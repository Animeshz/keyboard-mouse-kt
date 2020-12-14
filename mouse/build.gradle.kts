@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.konan.target.HostManager

val ideaActive = System.getProperty("idea.active") == "true"

val mainSourceSets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()
val testSourceSets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()

fun KotlinMultiplatformExtension.configureLinux() {
    linuxX64 { mavenPublication { artifactId = "${project.name}-kt-linuxx64" } }

    val linuxX64Main by sourceSets.getting
    val linuxX64Test by sourceSets.getting { dependsOn(linuxX64Main) }
    mainSourceSets.add(linuxX64Main)
    testSourceSets.add(linuxX64Test)
}

fun KotlinMultiplatformExtension.configureMingw() {
    mingwX64 { mavenPublication { artifactId = "${project.name}-kt-mingwx64" } }

    val mingwX64Main by sourceSets.getting
    val mingwX64Test by sourceSets.getting { dependsOn(mingwX64Main) }
    mainSourceSets.add(mingwX64Main)
    testSourceSets.add(mingwX64Test)
}

kotlin {
    if (ideaActive) {
        when {
            HostManager.hostIsLinux -> configureLinux()
            HostManager.hostIsMingw -> configureMingw()
        }
    } else {
        configureLinux()
        configureMingw()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependsOn(commonMain)
        }

        configure(mainSourceSets) {
            dependsOn(commonMain)
        }
        configure(testSourceSets) {
            dependsOn(commonTest)
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    explicitApi()
}
