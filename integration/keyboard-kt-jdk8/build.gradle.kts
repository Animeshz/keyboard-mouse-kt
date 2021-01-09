import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.2-native-mt")
    api(project(":keyboard-kt"))
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

afterEvaluate {
    publishing {
        val projectUrl = "https://github.com/Animeshz/keyboard-mouse-kt"

        repositories {
            maven {
                setUrl("https://api.bintray.com/maven/animeshz/maven/keyboard-kt-jdk8/;publish=1;override=1")
                credentials {
                    username = System.getenv("BINTRAY_USER")
                    password = System.getenv("BINTRAY_KEY")
                }
            }
        }

        publications.withType<MavenPublication> {
            artifactId = project.name
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
        }
    }
}
