@file:Suppress("UNUSED_VARIABLE")

import io.github.animeshz.keyboard_mouse.native_compile.Target
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("multiplatform")
    id("keyboard-mouse-native-compile")
    id("keyboard-mouse-publishing")
    id("lt.petuska.npm.publish") version "1.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    jvm()
    js(IR) {
        moduleName = "keyboard-mouse-kt"

        useCommonJs()
        nodejs()
        binaries.library()
    }

    val linuxSettings: org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.() -> Unit = {
        val main by compilations.getting

        main.cinterops.create("device") { defFile("src/linuxCommonMain/cinterop/device.def") }
        main.cinterops.create("x11") { defFile("src/linuxCommonMain/cinterop/x11.def") }
    }
    // https://github.com/touchlab/Stately/issues/62
    linuxX64(configure = linuxSettings)
    // linuxArm64(configure = linuxSettings)
    linuxArm32Hfp(configure = linuxSettings)

    mingwX64()
    // https://github.com/touchlab/Stately/issues/62
    // mingwX86()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                implementation("co.touchlab:stately-isolate:1.1.7-a1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by sourceSets.getting { dependsOn(commonMain) }
        val jvmTest by sourceSets.getting { dependsOn(commonTest) }

        val jsMain by sourceSets.getting { dependsOn(commonMain) }
        val jsTest by sourceSets.getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val linuxCommonMain by sourceSets.creating { dependsOn(commonMain) }
        val linuxCommonTest by sourceSets.creating { dependsOn(commonTest) }

        val linuxX64Main by sourceSets.getting { dependsOn(linuxCommonMain) }
        // val linuxArm64Main by sourceSets.getting { dependsOn(linuxCommonMain) }
        val linuxArm32HfpMain by sourceSets.getting { dependsOn(linuxCommonMain) }
        val linuxX64Test by sourceSets.getting { dependsOn(linuxCommonTest) }
//         val linuxArm64Test by sourceSets.getting { dependsOn(linuxCommonTest) }
        val linuxArm32HfpTest by sourceSets.getting { dependsOn(linuxCommonTest) }

        val mingwCommonMain by sourceSets.creating { dependsOn(commonMain) }
        val mingwCommonTest by sourceSets.creating { dependsOn(commonTest) }

        val mingwX64Main by sourceSets.getting { dependsOn(mingwCommonMain) }
        // val mingwX86Main by sourceSets.getting { dependsOn(mingwCommonMain) }
        val mingwX64Test by sourceSets.getting { dependsOn(mingwCommonTest) }
        // val mingwX86Test by sourceSets.getting { dependsOn(mingwCommonTest) }

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }

    explicitApi()
}

publishingConfig {
    repository = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
    username = System.getenv("SONATYPE_USER")
    password = System.getenv("SONATYPE_KEY")
}

npmPublishing {
    readme = project.rootProject.file("README.md")

    repositories {
        repository("npmjs") {
            registry = uri("https://registry.npmjs.org")
            authToken = System.getenv("NPM_TOKEN")
        }
    }

    publications {
        val js by getting {
            files {
                from(project.file("build/napi"))
            }

            bundleKotlinDependencies = false
            shrinkwrapBundledDependencies = false
            packageJsonTemplateFile = project.file("src/jsMain/package.template.json")
            packageJson {
                version = project.version as String
            }
        }
    }
}

nativeCompilation {
    dockerImage = "animeshz/keyboard-mouse-kt:latest"

    jni {
        headers {
            inputDir = "src/jvmMain/kotlin"
            outputDir = "src/jvmMain/generated/jni"
        }
        compilation {
            baseInputPaths = listOf("src/jvmMain/cpp", "src/nativeCommon")
            outputDir = "build/jni"
            targets = listOf(
                Target("windows", "x64", "source windows64"),
                Target("windows", "x86", "source windows32"),
                Target("linux", "x64", "source linux64"),
                Target("linux", "x86", "source linux32")
            )
        }
    }

    napi {
        baseInputPaths = listOf("src/jsMain/cpp", "src/nativeCommon")
        outputDir = "build/napi"

        targets = listOf(
            Target("windows", "x64", "source windows64"),
            Target("windows", "x86", "source windows32"),
            Target("linux", "x64", "source linux64")
            // NodeJS doesn't ship in x86, so people must be building the nodejs their selves, so supporting it is not really necessary for now
            // Target("linux", "x86", "source linux32")
        )
    }
}

tasks.withType<AbstractTestTask> {
    testLogging {
        showStackTraces = true
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.withType<KotlinJsCompile> {
    kotlinOptions.sourceMap = false
}

tasks.getByName<Test>("jvmTest") {
    useJUnitPlatform()
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    filter {
        exclude("**/cinterop/**")
    }
}
