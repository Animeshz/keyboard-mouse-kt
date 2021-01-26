package com.github.animeshz.keyboard_mouse.native_compile

class Target(
    val os: String,
    val arch: String,
    val dockerImage: String
)

open class JniConfiguration {
    val headers: JniHeaderConfiguration = JniHeaderConfiguration()
    val compilation: JniCompilationConfiguration = JniCompilationConfiguration()

    fun headers(configuration: JniHeaderConfiguration.() -> Unit) {
        headers.apply(configuration)
    }

    fun compilation(configuration: JniCompilationConfiguration.() -> Unit) {
        compilation.apply(configuration)
    }
}

open class JniHeaderConfiguration {
    var inputDir: String = ""
    var outputDir: String = ""
}

open class JniCompilationConfiguration {
    var baseInputPaths: List<String> = emptyList()
    var outputDir: String = ""
    var targets: List<Target> = emptyList()
}
