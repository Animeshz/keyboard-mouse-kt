plugins {
    kotlin("multiplatform") version "1.5.20-dev-5753" apply false
    id("io.codearte.nexus-staging") version "0.30.0"
}

allprojects {
    this.group = "io.github.animeshz"
    this.version = "0.3.3"

    repositories {
        mavenCentral()
        jcenter()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    }
}

subprojects {
    subprojects {
        // Address https://github.com/gradle/gradle/issues/4823: Force parent project evaluation before sub-project evaluation for Kotlin build scripts
        if (gradle.startParameter.isConfigureOnDemand
            && buildscript.sourceFile?.extension?.toLowerCase() == "kts"
            && parent != rootProject) {
            generateSequence(parent) { project -> project.parent.takeIf { it != rootProject } }
                .forEach { evaluationDependsOn(it.path) }
        }
    }
}

nexusStaging {
    username = System.getenv("SONATYPE_USER")
    password = System.getenv("SONATYPE_KEY")
    packageGroup = group as String
}
