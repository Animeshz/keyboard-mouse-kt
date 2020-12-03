package com.github.animeshz.keyboard.internal

import com.github.animeshz.keyboard.ExperimentalKeyIO
import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyEventType
import kotlin.native.concurrent.AtomicInt
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
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
import platform.posix.O_RDWR
import platform.posix.close
import platform.posix.open
import platform.posix.read
import platform.posix.write
import struct.input_event

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
object LinuxKeyboardHandler : NativeKeyboardHandler {
    private val inputFile = AtomicInt(0)
    private val worker = Worker.start(errorReporting = true, name = "LinuxKeyboardHandler")
    private val eventsInternal = MutableSharedFlow<KeyEvent>(extraBufferCapacity = 8)

    /**
     * A [SharedFlow] of [KeyEvent] for receiving Key events from the target platform.
     */
    override val events: SharedFlow<KeyEvent> get() = eventsInternal.asSharedFlow()

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

    /**
     * Sends the [keyEvent] to the platform.
     */
    override fun sendEvent(keyEvent: KeyEvent) {
        prepare()

        memScoped {
            val buffer = alloc<input_event>().apply {
                type = EV_KEY
                value = if (keyEvent.type == KeyEventType.KeyDown) LINUX_KEY_DOWN else LINUX_KEY_UP
                code = keyEvent.key.keyCode.toUShort()
            }

            write(inputFile.value, buffer.ptr, sizeOf<input_event>().toULong())
        }

        if (eventsInternal.subscriptionCount.value == 0) cleanup()
    }

    // ==================================== Internals ====================================
    private const val EV_KEY: UShort = 1U
    private const val LINUX_KEY_UP = 0U
    private const val LINUX_KEY_DOWN = 1U

    private fun prepare() {
        if (inputFile.value != 0) return
        inputFile.value = open("/dev/uinput", O_RDWR)
    }

    private fun readEvents() {
        memScoped {
            // Reading into input_event: https://stackoverflow.com/a/16695758/11377112
            val buffer = alloc<input_event>()

            while (eventsInternal.subscriptionCount.value != 0) {
                read(inputFile.value, buffer.ptr, sizeOf<input_event>().toULong())
                if (buffer.type != EV_KEY) continue

                val keyEventType = if (buffer.value == LINUX_KEY_UP) KeyEventType.KeyUp else KeyEventType.KeyDown
                process(keyEventType, buffer.code.toInt())
            }
        }
    }

    private fun cleanup() {
        val ip = inputFile.value
        if (ip != 0) {
            close(ip)
            inputFile.value = 0
        }
    }

    /**
     * Processes the event.
     */
    // TODO("Add support for extended key parsing")
    private fun process(keyEventType: KeyEventType, code: Int) {
        val key = Key.fromKeyCode(code)
        eventsInternal.tryEmit(KeyEvent(key, keyEventType))
    }

}

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return LinuxKeyboardHandler
}
