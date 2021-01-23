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
        implementationClass = "com.github.animeshz.keyboard_mouse.publishing.PublishingPlugin"
    }

    plugins.register("keyboard-mouse-configuration") {
        id = "keyboard-mouse-configuration"
        implementationClass = "com.github.animeshz.keyboard_mouse.configuration.ConfigurationPlugin"
    }

//    plugins.register("keyboard-mouse-multiplatform-configuration") {
//        id = "keyboard-mouse-multiplatform-plugin"
//        implementationClass = "com.github.animeshz.keyboard_mouse.multiplatform_configuration.MppConfigurationPlugin"
//    }

//    plugins.register()
}
