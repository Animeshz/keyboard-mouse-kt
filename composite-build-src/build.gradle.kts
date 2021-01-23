plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    jcenter()
}

gradlePlugin {
    plugins.register("keyboard-mouse-publishing") {
        id = "keyboard-mouse-publishing"
        implementationClass = "com.github.animeshz.keyboard_mouse.publishing.PublishingPlugin"
    }

    plugins.register("class-loader-plugin") {
        id = "class-loader-plugin"
        implementationClass = "com.example.ClassLoaderPlugin"
    }

//    plugins.register()
}
