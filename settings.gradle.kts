rootProject.name = "keyboard-mouse-kt"

fun module(path: String) {
    val name = path.substringAfterLast('/')
    include(name)
    project(":$name").projectDir = file(path)
}

includeBuild("composite-build-src")

module("keyboard-kt")
module("integration/keyboard-kt-jdk8")
// include("mouse-kt")
