# Contributing to KeyboardMouse.kt

👍🎉 First off, thanks for taking the time to contribute! 🎉👍

When contributing to this repository, please first discuss the change you wish to make via issue or github discussions.

__This project is on hold, I'm busy on my own (I have my entrance exams in few months, so I won't be able to manage
this). I've created a CONTRIBUTING.md if somebody wants to keep it alive, along with future plans. Feel free to create
issues or PRs if issues are small I'll try to solve them and for PRs I may merge.__

### How is the project organized?

The project (keyboard) is divided into 4 modules right now, common, jvm (on jvm branch currently), linuxX64, and
mingwX64.

The commonMain is where all the public interfaces and classes are defined almost everything about it is documented in
the [README.md](https://github.com/Animeshz/keyboard-mouse-kt/blob/master/README.md) under the Usages section and
there's
a [commonTest](https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonTest/kotlin/com/github/animeshz/keyboard)
module where interactive tests are written (they're not unit tests as mocking is not supported in K/N, but rather are
tests where you can run and test their behavior and print to the console, etc)

While the other are platform specific implementation. The JVM one is incomplete (nothing is done), while linuxX64 and
mingwX64 are done (tested mingw, linux may contain bugs but almost everything is good now as I partially tested on a VM)
.

The mingwX64 currently is implemented through Win32 API (which really isn't a 32 bit API), linuxX64 is implemented using
XLib and XInput2.

### Future Plans

Following are the future plans for the project:

- Migrate to dynamic library linking for linux (using dlopen/dlsym/dlclose from Posix API), see [#1][1] for more
  details.
- Create gradle task in to generate JNI headers for Kotlin external functions. Possibly by putting the task between
  compile-task and jar packaging task.
  References: [Kotlin replacement for javah](https://stackoverflow.com/q/48816188/11377112)
  , [How to solve missing javah in Java 10 – ugly way](https://www.owsiak.org/how-to-solve-missing-javah-ugly-way)
- Implement JNI each for different platforms, and compile the sources using cpp-library plugin (already setup in jvm
  branch), and then package it up in the resulting Jar by editing the source sets. I've considered it to do via C++
  instead of reusing Kotlin/Native because it will result in low performance and maybe huge sizes (if K/N becomes stable
  and performance wise equivalent we can directly reuse the sources we've written).
- Add Linux Device (`/dev/uinput` | `/dev/input/xxx`) based implementation of interaction of Keyboard/Mouse as a
  fallback when X11 is not present (after resolving [#1][1]).
- Implement Mouse API in similar way keyboard is implemented.

### Testing and building

To build and publish to mavenLocal:
`$ ./gradlew build publishToMavenLocal`

<sub>Note: When building for linux, you need libX11 and XInput2 (for Ubuntu: `apt install libX11-dev liblxi-dev`, for
Arch: `pacman -S libx11 libxi`), this soon is not going to be requirement see [#1][1]</sub>

[1]: https://github.com/Animeshz/keyboard-mouse-kt/issues/1
