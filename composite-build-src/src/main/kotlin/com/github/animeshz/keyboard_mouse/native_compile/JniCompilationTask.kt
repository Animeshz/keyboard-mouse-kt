package com.github.animeshz.keyboard_mouse.native_compile

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * For building shared libraries out of C/C++ sources for JVM
 */
open class JniCompilationTask @Inject constructor(
    private val targets: List<Target>,
    private val dockerImage: String
) : DefaultTask() {
    init {
        group = "nativeCompilation"
    }

    @get:Input
    @set:Option(option = "verbose", description = "Sets verbosity of output.")
    var isVerbose: Boolean = false

    @TaskAction
    fun run() {
        project.checkDockerInstallation()

        val tmpVar = project.file(".").absolutePath
        val path = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            "/run/desktop/mnt/host/${tmpVar[0].toLowerCase()}${tmpVar.substring(2 until tmpVar.length).replace('\\', '/')}"
        } else {
            tmpVar
        }

        for (target in targets) {
            work(path, target)
        }
    }

    private fun work(path: String, target: Target) {
        val errorStream = ByteArrayOutputStream()
        val interceptors = listOf(System.err, errorStream)

        val exitCode = MultiplexOutputStream(interceptors).use {
            project.exec {
                val command = buildString(capacity = 800) {
                    append("mkdir -p \$WORK_DIR/project/build/jni && ")
                    append("echo ==================================== && ")
                    append("echo Building ${target.os}-${target.arch} && ")
                    append("${target.preRunScript} && ")
                    append("mkdir -p \$WORK_DIR/project/build/tmp/compile-jni-${target.os}-${target.arch} && ")
                    append("cd \$WORK_DIR/project/build/tmp/compile-jni-${target.os}-${target.arch} && ")
                    append("\$CMAKE -DARCH=${target.arch} \$WORK_DIR/project/src/jvmMain/cpp/${target.os} && ")
                    append("\$CMAKE --build . ${if (isVerbose) "--verbose" else ""} --config Release && ")
                    append("cp -rf libKeyboardKt${target.arch}.{dll,so,dylib} \$WORK_DIR/project/build/jni 2>/dev/null || : && ")
                    append("cd .. && rm -rf compile-jni-${target.os}-${target.arch}")
                }

                commandLine(
                    "docker", "run", "--rm", "-v", "$path:/work/project", "-v", "/etc/localtime:/etc/localtime:ro",  dockerImage, "bash", "-c", command
                )

                isIgnoreExitValue = true
                standardOutput = System.out
                errorOutput = it
            }.exitValue
        }

        val errorOutput = errorStream.toString()

        if (Os.isFamily(Os.FAMILY_WINDOWS) && errorOutput.startsWith("docker: error during connect: This error may indicate that the docker daemon is not running.")) {
            // Fix non-daemon docker on Docker for Windows
            project.exec { commandLine("C:\\Program Files\\Docker\\Docker\\DockerCli.exe", "-SwitchDaemon") }.assertNormalExitValue()
            Thread.sleep(500)

            return work(path, target)
        } else if ("Clock skew detected" in errorOutput) {
            throw GradleException("Docker's time does not match with host's file system time. Please restart docker from system tray or docker-machine and run the build again.")
        }

        if (exitCode != 0) throw GradleException("An error occured while running the command, see the stderr for more details.")
    }
}
