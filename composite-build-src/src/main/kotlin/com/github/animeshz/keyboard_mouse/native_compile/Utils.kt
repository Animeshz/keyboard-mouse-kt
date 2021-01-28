package com.github.animeshz.keyboard_mouse.native_compile

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
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
