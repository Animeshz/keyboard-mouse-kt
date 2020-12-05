package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyEventType
import device.input_event
import kotlin.native.concurrent.AtomicInt
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import platform.posix.O_RDWR
import platform.posix.close
import platform.posix.ioctl
import platform.posix.open
import platform.posix.read
import platform.posix.write

@ExperimentalUnsignedTypes
@ExperimentalKeyIO
internal object DeviceKeyboardHandler : LinuxKeyboardHandlerBase() {
    private val inputFile = AtomicInt(0)

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
    private const val UI_SET_EVBIT: ULong = 1074025828U
    private const val UI_SET_KEYBIT: ULong = 1074025829U

    private const val LINUX_KEY_UP = 0U
    private const val LINUX_KEY_DOWN = 1U

    override fun prepare() {
        if (inputFile.value != 0) return
        val ip = open("/dev/uinput", O_RDWR)

        ioctl(ip, UI_SET_EVBIT, EV_KEY)
        for (i in 0 until 256) ioctl(ip, UI_SET_KEYBIT, i)

        memScoped {

        }

        inputFile.value = ip
    }

    override fun readEvents() {
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

    override fun cleanup() {
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
