rootProject.name = "keyboard-mouse-kt"

fun module(path: String) {
    val name = path.substringAfterLast('/')
    include(name)
    project(":$name").projectDir = file(path)
}

module("keyboard-kt")
module("integration/keyboard-kt-jdk8")
// include("mouse-kt")
