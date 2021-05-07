# Status and Installation

## Status

- [ ] Keyboard
    - [ ] JVM
        - [X] Windows x86_64 (64 bit)
        - [X] Windows x86    (32 bit)
        - [X] Linux x86_64 (64 bit)
        - [X] Linux x86    (32 bit)
        - [ ] Linux Arm32
        - [ ] Linux Arm64
        - [ ] MacOS
    - [ ] JS
        - [X] Windows x86_64 (64 bit)
        - [X] Windows x86    (32 bit)
        - [X] Linux x86_64 (64 bit)
        - [ ] Linux x86    (32 bit)
        - [ ] Linux Arm32
        - [ ] Linux Arm64
        - [ ] MacOS
    - [ ] Native
        - [X] Windows x86_64 (64 bit)
        - [ ] Windows x86    (32 bit)
        - [X] Linux x86_64 (64 bit)
        - [ ] Linux x86    (32 bit)
        - [ ] Linux Arm32
        - [ ] Linux Arm64
        - [ ] MacOS
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
            implementation("io.github.animeshz:keyboard-kt-jvm:<version>")
            implementation("io.github.animeshz:mouse-kt-jvm:<version>")
        }
        ```

    === "Kotlin/JS"
        ```kotlin
        plugins {
            kotlin("js") version "<kotlin-version>"
        }

        repositories {
            maven(url = "https://dl.bintray.com/animeshz/maven")
        }

        dependencies {
            implementation("io.github.animeshz:keyboard-kt-js:<version>")
            implementation("io.github.animeshz:mouse-kt-js:<version>")
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
            implementation("io.github.animeshz:keyboard-kt-jvm:<version>")
            implementation("io.github.animeshz:keyboard-kt-jdk8:<version>")

            implementation("io.github.animeshz:mouse-kt-jvm:<version>")
            implementation("io.github.animeshz:mouse-kt-jdk8:<version>")
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
            js()  // IR not supported right now, but will be soon.
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
                        implementation("io.github.animeshz:keyboard-kt:<version>")
                        implementation("io.github.animeshz:mouse-kt:<version>")
                    }
                }
                
                // Or configure each-platform by the suffix such as -jvm, -linuxX64, etc.
                val jvmMain by getting {
                    dependsOn(commonMain)
                    dependencies {
                        implementation("io.github.animeshz:keyboard-kt-jvm:<version>")
                        implementation("io.github.animeshz:mouse-kt-jvm:<version>")
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
            implementation("io.github.animeshz:keyboard-kt-jvm:<version>")
            implementation("io.github.animeshz:mouse-kt-jvm:<version>")
        }
        ```

    === "Kotlin/JVM"
        ```groovy
        plugins {
            id "kotlin-js" version "<kotlin-version>"
        }

        repositories {
            maven { url "https://dl.bintray.com/animeshz/maven" }
        }

        dependencies {
            implementation("io.github.animeshz:keyboard-kt-js:<version>")
            implementation("io.github.animeshz:mouse-kt-js:<version>")
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
            implementation("io.github.animeshz:keyboard-kt-jvm:<version>")
            implementation("io.github.animeshz:keyboard-kt-jdk8:<version>")

            implementation("io.github.animeshz:mouse-kt-jvm:<version>")
            implementation("io.github.animeshz:mouse-kt-jdk8:<version>")
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
            js()  // IR not supported right now, but will be soon.
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
                        implementation("io.github.animeshz:keyboard-kt:<version>")
                        implementation("io.github.animeshz:mouse-kt:<version>")
                    }
                }
                
                // Or configure each-platform by the suffix such as -jvm, -linuxX64, etc.
                jvmMain {
                    dependsOn(commonMain)
                    dependencies {
                        implementation("io.github.animeshz:keyboard-kt-jvm:<version>")
                        implementation("io.github.animeshz:mouse-kt-jvm:<version>")
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
              <groupId>io.github.animeshz</groupId>
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
              <groupId>io.github.animeshz</groupId>
              <artifactId>keyboard-kt-jvm</artifactId>
              <version>0.2.2</version>
              <type>pom</type>
            </dependency>
            <dependency>
              <groupId>io.github.animeshz</groupId>
              <artifactId>keyboard-kt-jdk8</artifactId>
              <version>0.2.2</version>
              <type>pom</type>
            </dependency>
          </dependencies>

        </project>
        ```

=== "NPM/NodeJS (package.json)"

    === "JS/NodeJS"
        ```json
        {
            "name": "<project-name>",
            "version": "<project-version>",

            "dependencies": {
                "keyboard-kt": "^0.3.0"
            }
        }
        ```

## Use interactively with Jupyter Notebook

Don't have time to setup a Gradle/Maven project? No worries, roll up a [Kotlin's Jupyter Kernel](https://github.com/Kotlin/kotlin-jupyter), and use it as a REPL (it even has kotlin-autocompletion).

??? "If you don't already have jupyter or kotlin-kernel, click here"

    === "Installing JupyterLab"
        ```bash
        python3 -m pip install jupyterlab
        ```
    === "Installing Kotlin Kernel"
        ```bash
        git clone https://github.com/Kotlin/kotlin-jupyter.git && cd kotlin-jupyter && ./gradlew install && cd -
        ```

Get the quickstart notebook for quickly start using the library without any hassles.
```bash
curl https://raw.githubusercontent.com/Animeshz/keyboard-mouse-kt/master/docs/getting_started.ipynb -o getting_started.ipynb
```
<sup>**Note: If you are in windows assuming you're using powershell**</sup>

Start the jupyter lab with:
```bash
jupyter lab
```

A browser will open with current directory, open the `getting_started.ipynb` and start playing with it.

??? "If you're new to jupyer, click here"

    use `Ctrl + Enter` to run a cell, `Esc` to get in command mode, `X` to cut a cell, `Z` to undo, `B` to create a new cell below, and so on. Refer to jupyter docs for more info.

A simple demo gif:

![keyboard-kt-jupyter-sample.gif](https://i.imgur.com/QTVcMp1.gif)
