package com.github.animeshz.keyboard_mouse.native_compile

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.OutputStream
import kotlin.system.exitProcess

internal fun Project.checkDockerInstallation() {
    println("Checking docker installation")

    val exit = exec {
        commandLine(if (Os.isFamily(Os.FAMILY_WINDOWS)) listOf("cmd", "/c", "where", "docker") else listOf("which", "docker"))
        isIgnoreExitValue = true
    }.exitValue
    if (exit != 0) {
        println("Please install docker before running this task")
        exitProcess(1)
    }
}

/**
 * MultiplexOutputStream allows you to write to multiple output streams "at once".
 * It allows you to use one [OutputStream] writer to write to multiple [OutputStream]
 * without repeating yourself.
 */
class MultiplexOutputStream(private val outputStreams: List<OutputStream>) : OutputStream() {
    override fun write(b: Int) = outputStreams.forEach { it.write(b) }
    override fun write(b: ByteArray) = outputStreams.forEach { it.write(b) }
    override fun write(b: ByteArray, off: Int, len: Int) = outputStreams.forEach { it.write(b, off, len) }

    override fun flush() = outputStreams.forEach { it.flush() }
    override fun close() = outputStreams.forEach { it.close() }
}
