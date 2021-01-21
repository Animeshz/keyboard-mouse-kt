@file:Suppress("UNUSED_VARIABLE")

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.internal.jvm.Jvm
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import kotlin.system.exitProcess

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

val mainSourceSets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()
val testSourceSets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()

fun KotlinMultiplatformExtension.configureJvm() {
    jvm()
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    tasks.getByName<Test>("jvmTest") {
        useJUnitPlatform()
    }

    val jvmMain by sourceSets.getting
    val jvmTest by sourceSets.getting {
        dependsOn(jvmMain)
        dependencies {
            implementation(kotlin("test-junit5"))
            runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
            implementation("io.kotest:kotest-assertions-core:4.3.2")
        }
    }
    mainSourceSets.add(jvmMain)
    testSourceSets.add(jvmTest)

    // JNI-C++ configuration
    val jniHeaderDirectory = file("src/jvmMain/generated/jni").apply { mkdirs() }
    jvmMain.resources.srcDir("build/jni")

    val generateJniHeaders by tasks.creating {
        group = "build"
        dependsOn(tasks.getByName("compileKotlinJvm"))

        // For caching
        inputs.dir("src/jvmMain/kotlin")
        outputs.dir("src/jvmMain/generated/jni")

        doLast {
            val javaHome = Jvm.current().javaHome
            val javap = javaHome.resolve("bin").walk().firstOrNull { it.name.startsWith("javap") }?.absolutePath ?: error("javap not found")
            val javac = javaHome.resolve("bin").walk().firstOrNull { it.name.startsWith("javac") }?.absolutePath ?: error("javac not found")
            val buildDir = file("build/classes/kotlin/jvm/main")
            val tmpDir = file("build/tmp/jvmJni").apply { mkdirs() }

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

                    val (packageName, className, methodInfo) =
                        """public \w*\s*class (.+)\.(\w+) (?:implements|extends).*\{\R([^\}]*)\}""".toRegex().find(output)?.destructured ?: return@forEach
                    val nativeMethods =
                        """.*\bnative\b.*""".toRegex().findAll(methodInfo).mapNotNull { it.groups }.flatMap { it.asSequence().mapNotNull { group -> group?.value } }.toList()
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
                        commandLine(javac, "-h", jniHeaderDirectory.absolutePath, outputFile.absolutePath)
                    }.assertNormalExitValue()
                }
        }
    }

    // For building shared libraries out of C/C++ sources
    val compileJni by tasks.creating {
        group = "build"
        dependsOn(generateJniHeaders)
        tasks.getByName("jvmProcessResources").dependsOn(this)

        // For caching
        inputs.dir("src/jvmMain/jni")
        outputs.dir("build/jni")

        doFirst {
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

        doLast {
            class Target(val os: String, val arch: String, val dockerImage: String)

            val targets = listOf(
                Target("windows", "x64", "animeshz/keyboard-mouse-kt:jni-build-windows-x64"),
                Target("windows", "x86", "animeshz/keyboard-mouse-kt:jni-build-windows-x86"),
                Target("linux", "x64", "animeshz/keyboard-mouse-kt:jni-build-linux-x64"),
                Target("linux", "x86", "animeshz/keyboard-mouse-kt:jni-build-linux-x86")
            )

            for (target in targets) {
                // Integrate with CMake
                val tmpVar = file(".").absolutePath
                val path = if (Os.isFamily(Os.FAMILY_WINDOWS)) "/run/desktop/mnt/host/${tmpVar[0].toLowerCase()}${tmpVar.substring(2 until tmpVar.length).replace('\\', '/')}"
                else tmpVar

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
                                    "cmake \$WORK_DIR/project/src/jvmMain/jni/${target.os}-${target.arch} && " +
                                    "cmake --build . --config Release && " +
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

                if (exit != 0) throw GradleException(error)
            }
        }
    }
}

fun KotlinMultiplatformExtension.configureJs() {
    js(IR) {
        nodejs()
    }

    val jsMain by sourceSets.getting {
        dependencies {
            implementation(devNpm("node-addon-api", "*"))
        }
    }
    val jsTest by sourceSets.getting { dependsOn(jsMain) }

    mainSourceSets.add(jsMain)
    testSourceSets.add(jsTest)
}

fun KotlinMultiplatformExtension.configureLinux() {
    linuxX64 {
        val main by compilations.getting

        main.cinterops.create("device") { defFile("src/linuxX64Main/cinterop/device.def") }
        main.cinterops.create("x11") { defFile("src/linuxX64Main/cinterop/x11.def") }
    }

    val linuxX64Main by sourceSets.getting
    val linuxX64Test by sourceSets.getting { dependsOn(linuxX64Main) }
    mainSourceSets.add(linuxX64Main)
    testSourceSets.add(linuxX64Test)
}

fun KotlinMultiplatformExtension.configureMingw() {
    mingwX64()

    val mingwX64Main by sourceSets.getting
    val mingwX64Test by sourceSets.getting { dependsOn(mingwX64Main) }
    mainSourceSets.add(mingwX64Main)
    testSourceSets.add(mingwX64Test)
}

kotlin {
    configureJvm()
    configureJs()
    configureLinux()
    configureMingw()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2-native-mt")
                implementation("org.jetbrains.kotlinx:atomicfu:0.14.4")
            }
        }
        val commonTest by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.mockk:mockk-common:1.10.3")
                implementation("io.kotest:kotest-assertions-core:4.4.0.RC2")
            }
        }

        configure(mainSourceSets) {
            dependsOn(commonMain)
        }
        configure(testSourceSets) {
            dependsOn(commonTest)
        }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    explicitApi()
}

afterEvaluate {
    publishing {
        val projectUrl = "https://github.com/Animeshz/keyboard-mouse-kt"

        repositories {
            maven {
                setUrl("https://api.bintray.com/maven/animeshz/maven/keyboard-kt/;publish=1;override=1")
                credentials {
                    username = System.getenv("BINTRAY_USER")
                    password = System.getenv("BINTRAY_KEY")
                }
            }
        }

        publications.withType<MavenPublication> {
            pom {
                name.set(project.name)
                version = project.version as String
                description.set("A multiplatform kotlin library for interacting with global keyboard and mouse events.")
                url.set(projectUrl)

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("$projectUrl/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("Animeshz")
                        name.set("Animesh Sahu")
                        email.set("animeshsahu19@yahoo.com")
                    }
                }

                scm {
                    url.set(projectUrl)
                    connection.set("scm:git:$projectUrl.git")
                    developerConnection.set("scm:git:git@github.com:Animeshz/keyboard-mouse-kt.git")
                }
            }

            val publication = this@withType
            if (publication.name == "kotlinMultiplatform") {
                publication.artifactId = project.name
            } else {
                publication.artifactId = "${project.name}-${publication.name}"
            }
        }
    }
}

tasks.withType<AbstractTestTask> {
    testLogging {
        showStackTraces = true
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    filter {
        exclude("**/cinterop/**")
    }
}