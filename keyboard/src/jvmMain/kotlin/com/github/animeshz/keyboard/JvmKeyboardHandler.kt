package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.File
import java.nio.file.FileSystems
import java.util.Locale

@ExperimentalKeyIO
internal object JvmKeyboardHandler : NativeKeyboardHandler {
    private val eventsInternal = MutableSharedFlow<KeyEvent>(extraBufferCapacity = 8)

    override val events: SharedFlow<KeyEvent>
        get() = TODO("Not yet implemented")

    override fun sendEvent(keyEvent: KeyEvent) {
        TODO("Not yet implemented")
    }

    override fun getKeyState(key: Key): KeyState {
        TODO("Not yet implemented")
    }

    external override fun isCapsLockOn(): Boolean

    external override fun isNumLockOn(): Boolean

    external override fun isScrollLockOn(): Boolean

    init {
        val os = System.getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH)
        val nativeLibFileName = when {
            "mac" in os || "darwin" in os -> error("Mac is not supported currently") /* "libKeyboardKt.dylib" */
            "win" in os -> "libKeyboardKt.dll"
            "nux" in os || "nix" in os || "aix" in os -> "libKeyboardKt.so"
            else -> error("OS not supported")
        }

        val extractionFile = File(System.getProperty("user.home"))
            .resolve(".KeyboardMouseKt")
            .resolve("runtime")
            .apply { mkdirs() }
            .apply { deleteOnExit() }
            .resolve(nativeLibFileName)

        javaClass.getResourceAsStream(nativeLibFileName)?.use { input ->
            extractionFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("null")

        try {
            System.loadLibrary(extractionFile.absolutePath)
        } finally {
            if ("posix" in FileSystems.getDefault().supportedFileAttributeViews()) extractionFile.delete()
            else extractionFile.deleteOnExit()
        }
    }
}

@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return JvmKeyboardHandler
}

@ExperimentalKeyIO
public fun main() {
    JvmKeyboardHandler
}
