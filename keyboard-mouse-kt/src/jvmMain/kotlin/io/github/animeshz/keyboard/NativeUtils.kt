package io.github.animeshz.keyboard

import java.io.File
import java.nio.file.FileSystems
import java.util.Locale

internal object NativeUtils {
    private val suffix: String
    private val extension: String

    init {
        val os = System.getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH)
        val arch = System.getProperty("os.arch", "unknown")

        suffix = when {
            "aarch64" in arch || "arm" in arch -> error("Arm not supported") /* "arm" */
            "64" in arch -> "x64"
            "86" in arch || "32" in arch -> "x86"
            else -> error("CPU architecture not supported. Current CPU architecture: $arch")
        }

        extension = when {
            "mac" in os || "darwin" in os -> error("Mac is not supported currently") /* "libKeyboardKt.dylib" */
            "win" in os -> "dll"
            "nux" in os || "nix" in os || "aix" in os -> "so"
            else -> error("OS not supported. Current OS: $os")
        }
    }

    fun loadLibraryFromJar(baseName: String) {
        val nativeLibFileName = "lib$baseName$suffix.$extension"

        val extractionFile = File(System.getProperty("user.home"))
            .resolve(".KeyboardMouseKt")
            .resolve("runtime")
            .apply { mkdirs() }
            .apply { deleteOnExit() }
            .resolve(nativeLibFileName)

        javaClass.classLoader.getResourceAsStream(nativeLibFileName)?.use { input ->
            extractionFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Native libraries were not found in the Jar")

        try {
            System.load(extractionFile.absolutePath)
        } finally {
            if ("posix" in FileSystems.getDefault().supportedFileAttributeViews()) extractionFile.delete()
            else extractionFile.deleteOnExit()
        }
    }
}
