plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins.register("keyboard-mouse-publishing") {
        id = "keyboard-mouse-publishing"
        implementationClass = "io.github.animeshz.keyboard_mouse.publishing.PublishingPlugin"
    }

    plugins.register("keyboard-mouse-native-compile") {
        id = "keyboard-mouse-native-compile"
        implementationClass = "io.github.animeshz.keyboard_mouse.native_compile.NativeCompilationPlugin"
    }
}
