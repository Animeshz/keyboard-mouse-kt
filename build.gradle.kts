plugins {
    kotlin("multiplatform") version "1.4.20" apply false
    id("maven-publish")
}

allprojects {
    this.group = "com.github.animeshz"
    this.version = "0.0.1"

    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "kotlin-multiplatform")
    apply(plugin = "maven-publish")

    publishing {
        publications.withType<MavenPublication> {
            pom {
                name.set("keyboard-mouse-kt-${project.name}")
                description.set("A multiplatform library for listening to global keyboard and mouse events.")
                url.set("https://github.com/Animeshz/keyboard-mouse-kt")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/Animeshz/keyboard-mouse-kt")
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
            }

            val publication = this@withType
            if (publication.name == "kotlinMultiplatform") {
                publication.artifactId = "keyboard-mouse-kt-${project.name}"
            } else {
                publication.artifactId = "keyboard-mouse-kt-${project.name}-${publication.name}"
            }

        }
    }
}
