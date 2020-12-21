@file:Suppress("UNUSED_VARIABLE")

import org.gradle.internal.jvm.Jvm
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    id("cpp-library")
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

    configurations.matching {
        it.name.startsWith("cppCompile") || it.name.startsWith("nativeLink") || it.name.startsWith("nativeRuntime")
    }.all { extendsFrom(jniImplementation) }

    val jniHeaderDirectory = layout.buildDirectory.dir("jniHeaders")

    val generateJniHeaders by tasks.creating(Exec::class) {
        group = "build"

        //        if (HostManager.hostIsMingw) {
        //            commandLine(
        //                    "cmd", "/c", "'${Jvm.current().javaHome.canonicalPath}\\bin\\javah'",
        //                    "-d", "src/jvmMain/jni/generated",
        //                    "-classpath", "src/jvmMain/kotlin",
        //                    "com.github.animeshz.keyboard.jni.TestKt"
        //            )
        //        } else {
        //            commandLine(
        //                    "sh", "-c", "${Jvm.current().javaHome.canonicalPath}/bin/javah",
        //                    "-d", "src/jvmMain/jni/generated",
        //                    "-classpath", "src/jvmMain/kotlin",
        //                    "com.github.animeshz.keyboard.jni.TestKt"
        //            )
        //        }
    }

    library {
        binaries.configureEach {
            val compileTask = compileTask.get()

            compileTask.dependsOn(generateJniHeaders)
            compileTask.compilerArgs.addAll(jniHeaderDirectory.map { listOf("-I", it.asFile.canonicalPath) })
            compileTask.compilerArgs.addAll(compileTask.targetPlatform.map {
                listOf("-I", "${Jvm.current().javaHome.canonicalPath}/include") + when {
                    it.operatingSystem.isMacOsX ->
                        listOf("-I", "${Jvm.current().javaHome.canonicalPath}/include/darwin")
                    it.operatingSystem.isLinux ->
                        listOf("-I", "${Jvm.current().javaHome.canonicalPath}/include/linux")
                    it.operatingSystem.isWindows ->
                        listOf("/I", "${Jvm.current().javaHome.canonicalPath}/include/win32")
                    else -> emptyList()
                }
            })
        }
    }

    //    tasks.getByName<Test>("jvmTest") {
    //        val sharedLib = library.developmentBinary.get() as CppSharedLibrary
    //        dependsOn(sharedLib.linkTask)
    //        systemProperty("java.library.path", sharedLib.linkFile.get().asFile.parentFile)
    //    }
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
