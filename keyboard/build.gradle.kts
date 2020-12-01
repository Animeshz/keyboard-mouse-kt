@file:Suppress("UNUSED_VARIABLE")

kotlin {
    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":common"))
            }
        }
        val commonTest by getting {
            dependencies {
                api(project(":common"))
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
    }

    explicitApi()
}
