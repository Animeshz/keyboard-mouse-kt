@file:Suppress("UNUSED_VARIABLE")

import org.apache.tools.ant.taskdefs.condition.Os

kotlin {
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
                implementation("co.touchlab:stately-isolate:1.+")
                implementation("co.touchlab:stately-iso-collections:1.+")
            }
        }
        val commonTest by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.mockk:mockk-common:1.10.3")
                implementation("io.kotest:kotest-assertions-core:4.3.1")
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonMain)
        }
        val linuxX64Test by getting {
            dependsOn(linuxX64Main)
            dependsOn(commonTest)
        }

        val mingwX64Main by getting {
            dependsOn(commonMain)
        }
        val mingwX64Test by getting {
            dependsOn(mingwX64Main)
            dependsOn(commonTest)
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    explicitApi()
}

afterEvaluate {
    if (!Os.isFamily(Os.FAMILY_UNIX)) {
        tasks.all {
            if (name.toLowerCase().contains("linux")) {
                enabled = false
            }
        }
    }
}
