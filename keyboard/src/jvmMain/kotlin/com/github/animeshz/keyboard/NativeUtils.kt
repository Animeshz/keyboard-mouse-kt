package com.github.animeshz.keyboard

import java.io.File
import java.nio.file.FileSystems

internal object NativeUtils {
    fun loadLibraryFromJar(nativeLibFileName: String) {
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
