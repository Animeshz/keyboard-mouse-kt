package com.github.animeshz.keyboard_mouse.publishing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType

open class PublishingConfigurationExtension  {
    var repository: String? = null
    var username: String? = null
    var password: String? = null
}

class PublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val ext = target.extensions.create("publishingConfig", PublishingConfigurationExtension::class.java)

        target.afterEvaluate {
            val repository = ext.repository ?: error("publishingConfig.repository must not be null")

            target.extensions.configure<PublishingExtension>("publishing") {
                val projectUrl = "https://github.com/Animeshz/keyboard-mouse-kt"

                repositories {
                    maven {
                        setUrl(repository)
                        credentials {
                            username = ext.username
                            password = ext.password
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
    }
}
