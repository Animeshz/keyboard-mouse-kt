plugins {
    kotlin("multiplatform") version "1.4.21" apply false
    id("maven-publish")
}

allprojects {
    this.group = "com.github.animeshz"
    this.version = "0.1.0"

    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "kotlin-multiplatform")
    apply(plugin = "maven-publish")

    afterEvaluate {
        publishing {
            val projectUrl = "https://github.com/Animeshz/keyboard-mouse-kt"

            repositories {
                maven {
                    setUrl("https://api.bintray.com/maven/animeshz/maven/keyboard-mouse-kt/;publish=1;override=1")
                    credentials {
                        username = System.getenv("BINTRAY_USER")
                        password = System.getenv("BINTRAY_KEY")
                    }
                }
            }

            publications.withType<MavenPublication> {
                pom {
                    name.set("${project.name}-kt")
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
                    publication.artifactId = "${project.name}-kt"
                } else {
                    publication.artifactId = "${project.name}-kt-${publication.name}"
                }
            }
        }
    }
}
