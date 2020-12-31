# Contributing to KeyboardMouse.kt

👍🎉 First off, thanks for taking the time to contribute! 🎉👍

When contributing to this repository, please first discuss the change you wish to make via issue or github discussions.


### How is the project organized

The project (keyboard) is divided into 4 modules: common, jvm, linuxX64, and mingwX64.

The commonMain is where all the public interfaces and classes are defined and almost everything about it is documented in
the [README.md](https://github.com/Animeshz/keyboard-mouse-kt/blob/master/README.md) under the Usages section and
there's
a [commonTest](https://github.com/Animeshz/keyboard-mouse-kt/blob/master/keyboard/src/commonTest/kotlin/com/github/animeshz/keyboard)
module where you can find a few more examples, etc)

While the other modules are platform specific implementation.

The JVM one is implemented through JNI which requies Docker to build cross-platform the shared libraries. The implementation of K/Native and K/JVM are same.

LinuxX64 is implemented utilizing the XLib and XInput2, and linked dynamically (no need to install any headers for build). There is a plan to use the `/dev/uinput` or `/dev/input/eventX` devices as a fallback, see [#6](https://github.com/Animeshz/keyboard-mouse-kt/issues/6).

And lastly, MingwX64 is done through Win32 API.


### Future Plans

Following are the future plans for the project:

- [X] Migrate to dynamic library linking for linux (using dlopen/dlsym/dlclose from Posix API), see [#1][1] for more
  details.
  on [this commit](https://github.com/Animeshz/keyboard-mouse-kt/commit/92027738f2093b7cc71c4693bcbc565aec26d206).
- [X] Create gradle task in to generate JNI headers for Kotlin external functions. Possibly by putting the task between
  compile-task and jar packaging task.
  References: [Kotlin replacement for javah](https://stackoverflow.com/q/48816188/11377112)
  , [How to solve missing javah in Java 10 – ugly way](https://www.owsiak.org/how-to-solve-missing-javah-ugly-way)
- [X] Implement way to cross compile the C/C++ library from any OS to any OS and then package it up in the resulting
  Jar. Done with PR [#4](https://github.com/Animeshz/keyboard-mouse-kt/pull/4).
- [X] (Complete for Windows x64 currently) Implement JNI each for different platforms. I've considered it to do via C++ instead of reusing Kotlin/Native because
  it will result in low performance and maybe huge sizes (if K/N becomes stable and performance wise equivalent we can
  directly reuse the sources we've written).
- Add Linux Device (`/dev/uinput` | `/dev/input/xxx`) based implementation of interaction of Keyboard/Mouse as a
  fallback when X11 is not present (after resolving [#1][1]).
- Implement Mouse API in similar way keyboard is implemented.

### Testing and building

To build and publish to mavenLocal:
`$ ./gradlew build publishToMavenLocal`

The only requirement is to install Docker when building for JVM due to cross-compilation requirement of JNI native libs to be able to pack the full Jar from any platform that is supported cross-platform.

[1]: https://github.com/Animeshz/keyboard-mouse-kt/issues/1
