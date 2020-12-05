package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.events.KeyEvent
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import platform.posix.getenv
import platform.posix.geteuid

@ExperimentalKeyIO
internal abstract class LinuxKeyboardHandlerBase : NativeKeyboardHandler {
    private val worker: Worker = Worker.start(errorReporting = true, name = "LinuxKeyboardHandler")
    protected val eventsInternal: MutableSharedFlow<KeyEvent> = MutableSharedFlow(extraBufferCapacity = 8)

    /**
     * A [SharedFlow] of [KeyEvent] for receiving Key events from the target platform.
     */
    override val events: SharedFlow<KeyEvent>
        get() = eventsInternal.asSharedFlow()

    init {
        // When subscriptionCount increments from 0 to 1, setup the native hook.
        eventsInternal.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                .filter { it }
                .onEach {
                    worker.execute(mode = TransferMode.SAFE, { this }) { handler ->
                        handler.prepare()
                        handler.readEvents()
                        handler.cleanup()
                    }
                }
                .launchIn(CoroutineScope(Dispatchers.Unconfined))
    }

    protected abstract fun prepare()
    protected abstract fun readEvents()
    protected abstract fun cleanup()
}

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return when {
        getenv("DISPLAY") != null -> X11KeyboardHandler
        geteuid() == 0U -> DeviceKeyboardHandler  // Reading & Writing to /dev/uinput if we have root access
        else -> throw RuntimeException("Neither X11 is present nor root access is granted, cannot receive events.")
    }
}
