@file:Suppress("UNUSED_VARIABLE")

kotlin {
    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2-native-mt")
            }
        }
        val commonTest by getting {
            dependencies {
                api(kotlin("test-common"))
                api(kotlin("test-annotations-common"))
                api("io.mockk:mockk-common:1.10.3")
                api("io.kotest:kotest-assertions-core:4.3.1")
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
