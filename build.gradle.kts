plugins {
    kotlin("multiplatform") version "1.4.21" apply false
}

allprojects {
    this.group = "com.github.animeshz"
    this.version = "0.2.5"

    repositories {
        mavenCentral()
        jcenter()
    }
}
