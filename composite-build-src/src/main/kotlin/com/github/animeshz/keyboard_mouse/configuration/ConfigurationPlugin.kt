package com.github.animeshz.keyboard_mouse.configuration

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ConfigurationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply(plugin = "org.jetbrains.kotlin.multiplatform")
        val ext = target.extensions.create("configureJni", JniConfiguration::class.java)

        setup(target, ext)
    }

    private fun setup(project: Project, extension: JniConfiguration) {
        project.afterEvaluate {
            with(extension.headers) {
                if (inputDir.isEmpty() || outputDir.isEmpty()) return@afterEvaluate
            }

            val compileJniAll by project.tasks.creating { group = "jni" }
            project.tasks.getByName("jvmProcessResources") { dependsOn(compileJniAll) }

            val headersTask = project.tasks
                .register<JniHeaderGenerationTask>("generateJniHeaders", extension.headers).get()

            with(extension.compilation) {
                if (baseInputPath.isEmpty() || outputDir.isEmpty() || targets.isEmpty()) return@afterEvaluate
            }

            extension.compilation.targets.forEach { target ->
                project.tasks
                    .register<JniCompilationTask>("compileJni${target.os.capitalize()}${target.arch.capitalize()}", target)
                    .also { compileJniAll.dependsOn(it.get()) }
                    .configure {
                        dockerImage = target.dockerImage

                        inputs.dir(extension.compilation.baseInputPath / target.os)
                        outputs.dir(extension.compilation.outputDir)

                        dependsOn(headersTask)
                    }
            }

            project.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("jvmMain").resources.srcDir(extension.compilation.outputDir)
            }
        }
    }

    private operator fun String.div(other: String) = "$this/$other"
}
