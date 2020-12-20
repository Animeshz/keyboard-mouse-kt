package com.github.animeshz.keyboard

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

//@ExperimentalKeyIO  // https://youtrack.jetbrains.com/issue/KT-44007
@SharedImmutable
private val globalHandler: AtomicRef<NativeKeyboardHandler?> = atomic(null)

// Thread local copy to provide fast (cached) access
//@ExperimentalKeyIO  // https://youtrack.jetbrains.com/issue/KT-44007
@ThreadLocal
private var localCachedHandler: NativeKeyboardHandler? = null

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    val localCopy = localCachedHandler
    if (localCopy != null) return localCopy

    val globalCopy = globalHandler.value
    if (globalCopy != null) {
        localCachedHandler = globalCopy
        return globalCopy
    }

    val handler = X11KeyboardHandler.create()
        ?: DeviceKeyboardHandler.create()
        ?: error("Neither X11 and XInput2 is present nor root access is given, Cannot instantiate ")

    localCachedHandler = handler
    globalHandler.value = handler

    return handler
}
