package com.github.animeshz.keyboard_mouse.native_compile

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class NativeCompilationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply(plugin = "org.jetbrains.kotlin.multiplatform")
        val ext = target.extensions.create("nativeCompilation", NativeConfiguration::class.java)

        setupJni(target, ext.jni)
        setupNapi(target, ext.napi)
    }

    private fun setupJni(project: Project, extension: JniConfiguration) {
        project.afterEvaluate {
            with(extension.headers) {
                if (inputDir.isEmpty() || outputDir.isEmpty()) return@afterEvaluate
            }

            val compileJniAll by project.tasks.creating { group = "nativeCompilation" }
            project.tasks.getByName("jvmProcessResources") { dependsOn(compileJniAll) }

            val headersTask = project.tasks
                .register<JniHeaderGenerationTask>("generateJniHeaders", extension.headers).get()

            with(extension.compilation) {
                if (baseInputPaths.isEmpty() || outputDir.isEmpty() || targets.isEmpty()) return@afterEvaluate
            }

            extension.compilation.targets.forEach { target ->
                project.tasks
                    .register<JniCompilationTask>("compileJni${target.os.capitalize()}${target.arch.capitalize()}", target)
                    .also { compileJniAll.dependsOn(it.get()) }
                    .configure {
                        for (path in extension.compilation.baseInputPaths) inputs.dir(path / target.os)
                        outputs.dir(extension.compilation.outputDir)

                        dependsOn(headersTask)
                    }
            }

            project.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("jvmMain").resources.srcDir(extension.compilation.outputDir)
            }
        }
    }

    private fun setupNapi(project: Project, extension: JsCompilationConfiguration) {
        project.afterEvaluate {
            val compileNapiAll by project.tasks.creating { group = "nativeCompilation" }

            with(extension) {
                if (baseInputPaths.isEmpty() || outputDir.isEmpty() || targets.isEmpty()) return@afterEvaluate
            }

            extension.targets.forEach { target ->
                project.tasks
                    .register<NapiCompilationTask>("compileNapi${target.os.capitalize()}${target.arch.capitalize()}", target)
                    .also { compileNapiAll.dependsOn(it.get()) }
                    .configure {
                        for (path in extension.baseInputPaths) inputs.dir(path / target.os)
                        outputs.dir(extension.outputDir)
                    }
            }

            // Specify the js include dir
        }
    }

    private operator fun String.div(other: String) = "$this/$other"
}
