rootProject.name = "keyboard-mouse-kt"

fun module(path: String) {
    val name = path.substringAfterLast('/')
    include(name)
    project(":$name").projectDir = file(path)
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    }
}

includeBuild("composite-build-src")

module("keyboard-mouse-kt")
//module("integration/keyboard-mouse-kt-coroutines")
