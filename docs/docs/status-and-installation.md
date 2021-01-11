# Status and Installation

## Status

- [ ] Keyboard
    - [ ] Windows
        - [X] x86_64 (64 bit)
        - [ ] x86    (32 bit)
    - [ ] Linux
        - [X] x86_64 (64 bit)
        - [ ] x86    (32 bit)
    - [ ] MacOS
    - [ ] JVM
        - [X] Windows x86_64 (64 bit)
        - [X] Windows x86    (32 bit)
        - [X] Linux x86_64 (64 bit)
        - [X] Linux x86    (32 bit)
        - [ ] Linux Arm32
        - [ ] Linux Arm64
- [ ] Mouse
    - [ ] Windows
    - [ ] Linux
    - [ ] MacOS
    - [ ] JVM

## Installation
<p>
    <a href="https://github.com/Animeshz/keyboard-mouse-kt/releases">
        <img src="https://img.shields.io/github/release-date/Animeshz/keyboard-mouse-kt?style=flat-square&label=Latest%20Release" alt="Latest Release" />
    </a>
    <a href="https://bintray.com/animeshz/maven/keyboard-kt">
        <img src="https://img.shields.io/bintray/v/animeshz/maven/keyboard-kt?color=blue&style=flat-square" alt="Bintray Version">
    </a>
    <img src="https://img.shields.io/github/languages/code-size/Animeshz/keyboard-mouse-kt?style=flat-square" alt="Code Size"/>
    <a href="https://github.com/Animeshz/keyboard-mouse-kt/blob/master/LICENSE">
        <img src="https://img.shields.io/github/license/Animeshz/keyboard-mouse-kt?style=flat-square" alt="License" />
    </a>
</p>

=== "Gradle (build.gradle.kts)"

    === "Kotlin/JVM"

        ```kotlin
        plugins {
            kotlin("jvm") version "<kotlin-version>"
        }

        repositories {
            maven(url = "https://dl.bintray.com/animeshz/maven")
        }

        dependencies {
            implementation("com.github.animeshz:keyboard-kt-jvm:<version>")
            implementation("com.github.animeshz:mouse-kt-jvm:<version>")
        }
        ```
    
    === "Java/JVM"

        ```kotlin
        plugins {
            java
        }

        repositories {
            maven(url = "https://dl.bintray.com/animeshz/maven")
        }

        dependencies {
            // Using from Java 8 or above (with complete Java support)
            implementation("com.github.animeshz:keyboard-kt-jvm:<version>")
            implementation("com.github.animeshz:keyboard-kt-jdk8:<version>")

            implementation("com.github.animeshz:mouse-kt-jvm:<version>")
            implementation("com.github.animeshz:mouse-kt-jdk8:<version>")
        }   
        ```

    === "Kotlin/Multiplatform"

        ```kotlin
        plugins {
            kotlin("mutliplatform") version "<kotlin-version>"
        }

        repositories {
            maven(url = "https://dl.bintray.com/animeshz/maven")
        }

        kotlin {
            // Your targets
            jvm()
            mingwX64 {
                binaries { executable { entryPoint = "main" } }
            }
            linuxX64 {
                binaries { executable { entryPoint = "main" } }
            }

            // Dependency to the library
            sourceSets {
                // Either as common
                val commonMain by getting {
                    dependencies {
                        implementation(kotlin("stdlib-common"))
                        implementation("com.github.animeshz:keyboard-kt:<version>")
                        implementation("com.github.animeshz:mouse-kt:<version>")
                    }
                }
                
                // Or configure each-platform by the suffix such as -jvm, -linuxX64, etc.
                val jvmMain by getting {
                    dependsOn(commonMain)
                    dependencies {
                        implementation("com.github.animeshz:keyboard-kt-jvm:<version>")
                        implementation("com.github.animeshz:mouse-kt-jvm:<version>")
                    }
                }
            }
        }
        ```

=== "Gradle (build.gradle)"

    === "Kotlin/JVM"

        ```groovy
        plugins {
            id "kotlin-jvm" version "<kotlin-version>"
        }

        repositories {
            maven { url "https://dl.bintray.com/animeshz/maven" }
        }

        dependencies {
            implementation("com.github.animeshz:keyboard-kt-jvm:<version>")
            implementation("com.github.animeshz:mouse-kt-jvm:<version>")
        }
        ```
    
    === "Java/JVM"

        ```groovy
        plugins {
            java
        }

        repositories {
            maven { url "https://dl.bintray.com/animeshz/maven" }
        }

        dependencies {
            // Using from Java 8 or above (with complete Java support)
            implementation("com.github.animeshz:keyboard-kt-jvm:<version>")
            implementation("com.github.animeshz:keyboard-kt-jdk8:<version>")

            implementation("com.github.animeshz:mouse-kt-jvm:<version>")
            implementation("com.github.animeshz:mouse-kt-jdk8:<version>")
        }   
        ```

    === "Kotlin/Multiplatform"

        ```groovy
        plugins {
            id "kotlin-mutliplatform" version "<kotlin-version>"
        }

        repositories {
            maven { url "https://dl.bintray.com/animeshz/maven" }
        }

        kotlin {
            // Your targets
            jvm()
            mingwX64 {
                binaries { executable { entryPoint = "main" } }
            }
            linuxX64 {
                binaries { executable { entryPoint = "main" } }
            }

            // Dependency to the library
            sourceSets {
                // Either as common
                commonMain {
                    dependencies {
                        implementation(kotlin("stdlib-common"))
                        implementation("com.github.animeshz:keyboard-kt:<version>")
                        implementation("com.github.animeshz:mouse-kt:<version>")
                    }
                }
                
                // Or configure each-platform by the suffix such as -jvm, -linuxX64, etc.
                jvmMain {
                    dependsOn(commonMain)
                    dependencies {
                        implementation("com.github.animeshz:keyboard-kt-jvm:<version>")
                        implementation("com.github.animeshz:mouse-kt-jvm:<version>")
                    }
                }
            }
        }
        ```

=== "Maven (pom.xml)"

    === "Kotlin/JVM"

        ```xml
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
          <modelVersion>4.0.0</modelVersion>

          <repositories>
            <repository>
              <id>bintray-animeshz-maven</id>
              <name>bintray</name>
              <url>https://dl.bintray.com/animeshz/maven</url>
            </repository>
          </repositories>

          <dependencies>
            <dependency>
              <groupId>com.github.animeshz</groupId>
              <artifactId>keyboard-kt-jvm</artifactId>
              <version>0.2.2</version>
              <type>pom</type>
            </dependency>
          </dependencies>

        </project>
        ```
    
    === "Java/JVM"

        ```xml
        <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
          <modelVersion>4.0.0</modelVersion>

          <repositories>
            <repository>
              <id>bintray-animeshz-maven</id>
              <name>bintray</name>
              <url>https://dl.bintray.com/animeshz/maven</url>
            </repository>
          </repositories>

          <dependencies>
            <dependency>
              <groupId>com.github.animeshz</groupId>
              <artifactId>keyboard-kt-jvm</artifactId>
              <version>0.2.2</version>
              <type>pom</type>
            </dependency>
            <dependency>
              <groupId>com.github.animeshz</groupId>
              <artifactId>keyboard-kt-jdk8</artifactId>
              <version>0.2.2</version>
              <type>pom</type>
            </dependency>
          </dependencies>

        </project>
        ```
