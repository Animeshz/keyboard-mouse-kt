plugins {
    kotlin("multiplatform") version "1.4.21" apply false
}

allprojects {
    this.group = "com.github.animeshz"
    this.version = "0.2.4"

    repositories {
        mavenCentral()
        jcenter()
    }
}
