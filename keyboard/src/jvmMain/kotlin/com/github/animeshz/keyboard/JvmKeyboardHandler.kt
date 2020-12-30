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
        val arch = System.getProperty("os.arch", "unknown")

        val suffix = when {
            "aarch64" in arch || "arm" in arch -> error("Arm not supported") /* "arm" */
            "64" in arch -> "x64"
            "86" in arch || "32" in arch -> "x86"
            else -> error("CPU architecture not supported")
        }
        val extension = when {
            "mac" in os || "darwin" in os -> error("Mac is not supported currently") /* "libKeyboardKt.dylib" */
            "win" in os -> "dll"
            "nux" in os || "nix" in os || "aix" in os -> "so"
            else -> error("OS not supported")
        }

        NativeUtils.loadLibraryFromJar("libKeyboardKt$suffix.$extension")
    }
}

@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return JvmKeyboardHandler
}
