@file:Suppress("UNUSED_VARIABLE")

import org.gradle.internal.jvm.Jvm
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    `cpp-library`
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

val ideaActive = System.getProperty("idea.active") == "true"

val mainSourceSets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()
val testSourceSets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>()

fun KotlinMultiplatformExtension.configureJvm() {
    jvm {
        mavenPublication { artifactId = "${project.name}-kt-jvm" }
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    val jvmMain by sourceSets.getting
    val jvmTest by sourceSets.getting { dependsOn(jvmMain) }
    mainSourceSets.add(jvmMain)
    testSourceSets.add(jvmTest)

    // JNI-C++ configuration
    val jniImplementation by configurations.creating
    val jniHeaderDirectory = file("src/jvmMain/generated/jni").apply { mkdirs() }

    configurations.matching {
        it.name.startsWith("cppCompile") || it.name.startsWith("nativeLink") || it.name.startsWith("nativeRuntime")
    }.all { extendsFrom(jniImplementation) }

    val generateJniHeaders by tasks.creating {
        group = "build"
        dependsOn(tasks.getByName("jvmMainClasses"))
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
                            commandLine(javap, "-cp", buildDir.absolutePath, file.absolutePath)
                            standardOutput = it
                        }
                        it.toString()
                    }

                    val (packageName, className, methodInfo) =
                        """public \w*\s*class (.+)\.(\w+) \{\R([^\}]*)\}""".toRegex().find(output)?.destructured ?: return@forEach
                    val nativeMethods =
                        """.*\bnative\b.*""".toRegex().findAll(methodInfo).mapNotNull { it.groups }.flatMap { it.asSequence().mapNotNull { group -> group?.value } }.toList()
                    if (nativeMethods.isEmpty()) return@forEach

                    val source = buildString {
                        appendln("package $packageName;")
                        appendln("public class $className {")
                        for (method in nativeMethods) {
                            val updatedMethod = StringBuilder(method).apply {
                                var count = 0
                                for (i in indices) if (this[i] == ',' || this[i] == ')') insert(i, " arg${count++}")
                            }
                            appendln(updatedMethod)
                        }
                        appendln("}")
                    }
                    val outputFile = tmpDir.resolve(packageName.replace(".", "/")).apply { mkdirs() }.resolve("$className.java").apply { createNewFile() }
                    outputFile.writeText(source)

                    project.exec {
                        commandLine(javac, "-h", jniHeaderDirectory.absolutePath, outputFile.absolutePath)
                    }
                }
        }
    }

//    library {
//        println(this.name)
//        linkage.set(listOf(Linkage.SHARED))
//        privateHeaders.setFrom(jniHeaderDirectory.canonicalPath)
//        targetMachines.set(listOf(machines.linux.x86_64, machines.windows.x86, machines.windows.x86_64))
//
//        binaries.configureEach {
//            val compileTask = compileTask.get()
//
//            compileTask.dependsOn(generateJniHeaders)
//            tasks.getByName("jvmJar").dependsOn(compileTask)
//
////            compileTask.includes.from.add(jniHeaderDirectory.canonicalPath)
//            compileTask.includes.from.add("${Jvm.current().javaHome.canonicalPath}/include")
//
//            val currentOs = compileTask.targetPlatform.get().operatingSystem
//            when {
//                currentOs.isMacOsX -> compileTask.includes.from.add("${Jvm.current().javaHome.canonicalPath}/include/darwin")
//                currentOs.isLinux -> compileTask.includes.from.add("${Jvm.current().javaHome.canonicalPath}/include/linux")
//                currentOs.isWindows -> compileTask.includes.from.add("${Jvm.current().javaHome.canonicalPath}/include/win32")
//            }
//
//            compileTask.source.setFrom(fileTree("src/jvmMain/jni") { include("**/*.c", "**/*.cpp") })
//        }
//    }

    //            tasks.getByName<Test>("jvmTest") {
    //            val sharedLib = library.developmentBinary.get() as CppSharedLibrary
    //            dependsOn(sharedLib.linkTask)
    //            systemProperty("java.library.path", sharedLib.linkFile.get().asFile.parentFile)
    //        }
    //
//    tasks.getByName<Jar>("jvmJar") {
//        from(library.developmentBinary.flatMap { (it as CppSharedLibrary).linkFile })
//    }
}

fun KotlinMultiplatformExtension.configureLinux() {
    linuxX64 {
        val main by compilations.getting

        main.cinterops.create("device") { defFile("src/linuxX64Main/cinterop/device.def") }
        main.cinterops.create("x11") { defFile("src/linuxX64Main/cinterop/x11.def") }
        mavenPublication { artifactId = "${project.name}-kt-linuxX64" }
    }

    val linuxX64Main by sourceSets.getting
    val linuxX64Test by sourceSets.getting { dependsOn(linuxX64Main) }
    mainSourceSets.add(linuxX64Main)
    testSourceSets.add(linuxX64Test)
}

fun KotlinMultiplatformExtension.configureMingw() {
    mingwX64 { mavenPublication { artifactId = "${project.name}-kt-mingwX64" } }

    val mingwX64Main by sourceSets.getting
    val mingwX64Test by sourceSets.getting { dependsOn(mingwX64Main) }
    mainSourceSets.add(mingwX64Main)
    testSourceSets.add(mingwX64Test)
}

kotlin {
    configureJvm()
    configureLinux()
    configureMingw()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2-native-mt")
                implementation("org.jetbrains.kotlinx:atomicfu:0.14.4")
                implementation("co.touchlab:stately-isolate:1.1.1-a1")
                implementation("co.touchlab:stately-iso-collections:1.1.1-a1")
            }
        }
        val commonTest by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.mockk:mockk-common:1.10.3")
                implementation("io.kotest:kotest-assertions-core:4.3.1")
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

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    filter {
        exclude("**/cinterop/**")
    }
}
