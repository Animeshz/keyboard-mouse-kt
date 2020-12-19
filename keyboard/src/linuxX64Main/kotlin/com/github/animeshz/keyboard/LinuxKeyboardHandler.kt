package com.github.animeshz.keyboard

import platform.posix.getenv
import platform.posix.geteuid

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return when {
        getenv("DISPLAY") != null -> X11KeyboardHandler
        geteuid() == 0U -> TODO()
        else -> throw RuntimeException("X11 is not present/running in the host.")
    }
}
