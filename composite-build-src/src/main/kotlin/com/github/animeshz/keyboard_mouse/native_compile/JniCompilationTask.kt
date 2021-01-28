package com.github.animeshz.keyboard_mouse.native_compile

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * For building shared libraries out of C/C++ sources
 */
open class JniCompilationTask @Inject constructor(
    private val target: Target
) : DefaultTask() {
    init {
        group = "jni"
    }

    @get:Input
    @set:Option(option = "verbose", description = "Configures the URL to be verified.")
    var isVerbose: Boolean = false

    var dockerImage: String = ""

    private fun check() {
        println("Checking docker installation")

        val exit = project.exec {
            commandLine(if (Os.isFamily(Os.FAMILY_WINDOWS)) listOf("cmd", "/c", "where", "docker") else listOf("which", "docker"))
            isIgnoreExitValue = true
        }.exitValue
        if (exit != 0) {
            println("Please install docker before running this task")
            exitProcess(1)
        }
    }

    @TaskAction
    fun run() {
        check()

        val tmpVar = project.file(".").absolutePath
        val path = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            "/run/desktop/mnt/host/${tmpVar[0].toLowerCase()}${tmpVar.substring(2 until tmpVar.length).replace('\\', '/')}"
        } else {
            tmpVar
        }

        val work: () -> Pair<Int, String> = {
            ByteArrayOutputStream().use {
                project.exec {
                    commandLine(
                        "docker",
                        "run",
                        "--rm",
                        "-v",
                        "$path:/work/project",
                        target.dockerImage,
                        "bash",
                        "-c",
                        "mkdir -p \$WORK_DIR/project/build/jni && " +
                            "mkdir -p \$WORK_DIR/project/build/tmp/compile-jni-${target.os}-${target.arch} && " +
                            "cd \$WORK_DIR/project/build/tmp/compile-jni-${target.os}-${target.arch} && " +
                            "cmake -DARCH=${target.arch} \$WORK_DIR/project/src/jvmMain/cpp/${target.os} && " +
                            "cmake --build . ${if (isVerbose) "--verbose " else ""}--config Release && " +
                            "cp -rf libKeyboardKt${target.arch}.{dll,so,dylib} \$WORK_DIR/project/build/jni 2>/dev/null || : && " +
                            "cd .. && rm -rf compile-jni-${target.os}-${target.arch}"
                    )

                    isIgnoreExitValue = true
                    standardOutput = System.out
                    errorOutput = it
                }.exitValue to it.toString()
            }
        }
        var (exit, error) = work()

        // Fix non-daemon docker on Docker for Windows
        val nonDaemonError = "docker: error during connect: This error may indicate that the docker daemon is not running."
        if (Os.isFamily(Os.FAMILY_WINDOWS) && error.startsWith(nonDaemonError)) {
            project.exec { commandLine("C:\\Program Files\\Docker\\Docker\\DockerCli.exe", "-SwitchDaemon") }.assertNormalExitValue()

            do {
                Thread.sleep(500)
                val result = work()
                exit = result.first
                error = result.second
            } while (error.startsWith(nonDaemonError))
        }

        System.err.println(error)
        if (exit != 0) throw GradleException("An error occured while running the command, see the stderr for more details.")
    }
}
