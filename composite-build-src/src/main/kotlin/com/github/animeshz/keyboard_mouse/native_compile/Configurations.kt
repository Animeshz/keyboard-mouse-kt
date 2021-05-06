package com.github.animeshz.keyboard_mouse.native_compile

class Target(
    val os: String,
    val arch: String,
    val preRunScript: String
)

open class NativeConfiguration {
    val jni = JniConfiguration()
    val napi = JsCompilationConfiguration()
    var dockerImage: String = ""

    fun jni(configuration: JniConfiguration.() -> Unit) {
        jni.apply(configuration)
    }

    fun napi(configuration: JsCompilationConfiguration.() -> Unit) {
        napi.apply(configuration)
    }
}

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

open class JsCompilationConfiguration {
    var baseInputPaths: List<String> = emptyList()
    var outputDir: String = ""
    var targets: List<Target> = emptyList()
}
