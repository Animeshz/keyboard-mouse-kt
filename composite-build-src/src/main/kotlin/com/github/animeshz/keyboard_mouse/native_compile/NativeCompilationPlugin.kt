package com.github.animeshz.keyboard_mouse.native_compile

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

class NativeCompilationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply(plugin = "org.jetbrains.kotlin.multiplatform")
        val ext = target.extensions.create("nativeCompilation", NativeConfiguration::class.java)

        target.afterEvaluate {
            setupJni(target, ext.jni, ext.dockerImage)
            setupNapi(target, ext.napi, ext.dockerImage)
        }
    }

    private fun setupJni(project: Project, extension: JniConfiguration, dockerImage: String) {
        with(extension.headers) {
            if (inputDir.isEmpty() || outputDir.isEmpty()) return
        }

        val headersTask = project.tasks
            .register<JniHeaderGenerationTask>("generateJniHeaders", extension.headers).get()

        with(extension.compilation) {
            if (baseInputPaths.isEmpty() || outputDir.isEmpty() || targets.isEmpty()) return
        }

        val compileJni = project.tasks
            .register<JniCompilationTask>("compileJni", extension.compilation.targets, dockerImage).apply {
                configure {
                    inputs.dir(".")
                    outputs.dir(extension.compilation.outputDir)

                    dependsOn(headersTask)
                }
            }.get()


        project.tasks.withType<ProcessResources>().named("jvmProcessResources") {
            dependsOn(compileJni)
            from(project.file(extension.compilation.outputDir))
        }
    }

    private fun setupNapi(project: Project, extension: JsCompilationConfiguration, dockerImage: String) {
        with(extension) {
            if (baseInputPaths.isEmpty() || outputDir.isEmpty() || targets.isEmpty()) return
        }

        val compileNapi = project.tasks
            .register<NapiCompilationTask>("compileNapi", extension.targets, dockerImage)
            .apply {
                configure {
                    for (path in extension.baseInputPaths) for (target in extension.targets) inputs.dir(path / target.os)
                    outputs.dir(extension.outputDir)
                }
            }
            .get()

        project.tasks.withType<ProcessResources>().named("jsProcessResources") {
            dependsOn(compileNapi)
            from(project.file(extension.outputDir))
        }
    }

    private operator fun String.div(other: String) = "$this/$other"
}
