package com.github.animeshz.keyboard_mouse.native_compile

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.jvm.Jvm
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.system.exitProcess

open class JniHeaderGenerationTask @Inject constructor(
    private val configuration: JniHeaderConfiguration
) : DefaultTask() {
    val metaRegex = """public \w*\s*class (.+)\.(\w+) (?:implements|extends).*\{\R([^\}]*)\}""".toRegex()
    val methodRegex = """.*\bnative\b.*""".toRegex()

    init {
        group = "jni"

        inputs.dir(configuration.inputDir)
        outputs.dir(configuration.outputDir)

        dependsOn(project.tasks.getByName("compileKotlinJvm"))
    }

    @TaskAction
    fun run() {
        val javaHome = Jvm.current().javaHome
        val javap = javaHome.resolve("bin").walk().firstOrNull { it.name.startsWith("javap") }?.absolutePath ?: error("javap not found")
        val javac = javaHome.resolve("bin").walk().firstOrNull { it.name.startsWith("javac") }?.absolutePath ?: error("javac not found")
        val buildDir = project.file("build/classes/kotlin/jvm/main")
        val tmpDir = project.file("build/tmp/jvmJni").apply { mkdirs() }

        buildDir.walkTopDown()
            .filter { "META" !in it.absolutePath }
            .forEach { file ->
                if (!file.isFile) return@forEach

                val output = ByteArrayOutputStream().use {
                    project.exec {
                        commandLine(javap, "-private", "-cp", buildDir.absolutePath, file.absolutePath)
                        standardOutput = it
                    }.assertNormalExitValue()
                    it.toString()
                }

                val (packageName, className, methodInfo) = metaRegex.find(output)?.destructured ?: return@forEach
                val nativeMethods = methodRegex.findAll(methodInfo).mapNotNull { it.groups }.flatMap { it.asSequence().mapNotNull { group -> group?.value } }.toList()
                if (nativeMethods.isEmpty()) return@forEach

                val source = buildString {
                    appendln("package $packageName;")
                    appendln("public class $className {")
                    for (method in nativeMethods) {
                        if ("()" in method) appendln(method)
                        else {
                            val updatedMethod = StringBuilder(method).apply {
                                var count = 0
                                var i = 0
                                while (i < length) {
                                    if (this[i] == ',' || this[i] == ')') insert(i, " arg${count++}".also { i += it.length + 1 })
                                    else i++
                                }
                            }
                            appendln(updatedMethod)
                        }
                    }
                    appendln("}")
                }

                val outputFile = tmpDir.resolve(packageName.replace(".", "/")).apply { mkdirs() }.resolve("$className.java").apply { delete() }.apply { createNewFile() }
                outputFile.writeText(source)

                project.exec {
                    commandLine(javac, "-h", project.file(configuration.outputDir).absolutePath, outputFile.absolutePath)
                }.assertNormalExitValue()
            }
    }
}
