plugins {
    kotlin("multiplatform") version "1.4.20" apply false
}

allprojects {
    this.group = "com.github.animeshz"
    this.version = "0.0.1"

    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "kotlin-multiplatform")
}
