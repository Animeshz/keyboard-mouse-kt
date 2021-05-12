package io.github.animeshz.keyboard

import co.touchlab.stately.isolate.IsolateState
import io.github.animeshz.keyboard.entity.Key
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toCPointer
import platform.posix.SIGINT
import platform.posix.atexit
import platform.posix.exit
import platform.posix.signal
import platform.windows.CallNextHookEx
import platform.windows.DispatchMessageA
import platform.windows.GetCurrentThreadId
import platform.windows.GetKeyState
import platform.windows.GetLastError
import platform.windows.GetMessageW
import platform.windows.GetModuleHandleW
import platform.windows.HHOOK
import platform.windows.INPUT
import platform.windows.LLKHF_INJECTED
import platform.windows.LPARAM
import platform.windows.LRESULT
import platform.windows.MAPVK_VSC_TO_VK_EX
import platform.windows.MSG
import platform.windows.MapVirtualKeyA
import platform.windows.PostThreadMessageW
import platform.windows.SendInput
import platform.windows.SetWindowsHookExW
import platform.windows.Sleep
import platform.windows.TranslateMessage
import platform.windows.UnhookWindowsHookEx
import platform.windows.VK_PACKET
import platform.windows.WH_KEYBOARD_LL
import platform.windows.WM_KEYDOWN
import platform.windows.WM_QUIT
import platform.windows.WM_SYSKEYDOWN
import platform.windows.WPARAM
import platform.windows.tagKBDLLHOOKSTRUCT
import kotlin.js.ExperimentalJsExport
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@ExperimentalJsExport
@ExperimentalKeyIO
@ExperimentalUnsignedTypes
public actual object NativeKeyboard {
    private val idCount = AtomicInt(0)
    private val handlers = IsolateState { mutableMapOf<Int, KeyboardEventHandler>() }

    /**
     * Adds a [KeyboardEventHandler] to the event queue.
     * Returns unique id of the handler which can be used to cancel the subscription.
     */
    public actual fun addEventHandler(handler: KeyboardEventHandler): Int {
        handlers.access { it[idCount.value] = handler }
        return idCount.value++
    }

    /**
     * Removes handler having given unique [id]
     */
    public actual fun removeEventHandler(id: Int) {
        handlers.access { it.remove(id) }
    }

    /**
     * Sends the a [Key] event to the host.
     */
    public actual fun sendEvent(keyCode: Int, isPressed: Boolean) {
        if (keyCode !in Key.Esc.keyCode..Key.F24.keyCode) return

        memScoped {
            val input = alloc<INPUT>().apply {
                type = INPUT_KEYBOARD
                ki.time = 0U
                ki.dwExtraInfo = 0U

                val extended = when (keyCode) {
                    Key.RightCtrl.keyCode, Key.RightAlt.keyCode, Key.RightSuper.keyCode, Key.RightShift.keyCode -> 1U
                    else -> 0U
                }

                // Send Windows/Super key with virtual code, because there's no particular scan code for that.
                if (keyCode == Key.LeftSuper.keyCode) {
                    ki.wVk = 0x5B.toUShort()
                    ki.dwFlags = extended or if (isPressed) 0U else 2U
                } else {
                    ki.wScan = keyCode.toUShort()
                    ki.dwFlags = 8U or extended or if (isPressed) 0U else 2U
                }
            }

            SendInput(1, input.ptr, sizeOf<INPUT>().toInt())
        }
    }

    /**
     * Gets the current key state (if its pressed or not) from the host.
     *
     * For toggle states consider using [isCapsLockOn], [isNumLockOn] and [isScrollLockOn] for respective purpose.
     */
    public actual fun isPressed(keyCode: Int): Boolean {
        if (keyCode !in Key.Esc.keyCode..Key.F24.keyCode) return false

        val vk = if (keyCode == Key.LeftSuper.keyCode) 0x5B
        else MapVirtualKeyA(keyCode.toUInt(), MAPVK_VSC_TO_VK_EX).toInt()

        return GetKeyState(vk) < 0
    }

    public actual fun isCapsLockOn(): Boolean = GetKeyState(0x14).toInt() and 1 != 0

    public actual fun isNumLockOn(): Boolean = GetKeyState(0x90).toInt() and 1 != 0

    public actual fun isScrollLockOn(): Boolean = GetKeyState(0x91).toInt() and 1 != 0

    // ==================================== Internals ====================================
    internal const val FAKE_ALT: Int = LLKHF_INJECTED or 0x20
    private const val INPUT_KEYBOARD = 1U
    private val WINDOWS_VK_MAPPING = mapOf(
        0x21 to Key.PageUp,
        0x22 to Key.PageDown,
        0x23 to Key.End,
        0x24 to Key.Home,
        0x25 to Key.Left,
        0x26 to Key.Up,
        0x27 to Key.Right,
        0x28 to Key.Down,
        0x5B to Key.LeftSuper
    )

    private val worker = Worker.start(errorReporting = true, name = "NativeKeyboard")
    private val threadId = worker.execute(mode = TransferMode.SAFE, {}) { GetCurrentThreadId() }.result

    init {
        // Force execute cleanup handlers on SIGINT (Ctrl + C)
        signal(SIGINT, staticCFunction { _ -> exit(0) })

        atexit(
            staticCFunction { ->
                stopReadingEvents()
                worker.requestTermination(true).consume {}
            }
        )
    }

    /**
     * Polls the message queue in Windows to receive events in [lowLevelKeyboardProc].
     * When [MutableSharedFlow.subscriptionCount] of [eventsInternal] reduces to 0, breaks the procedure and exits.
     */
    private fun startReadingEvents() {
        worker.execute(mode = TransferMode.SAFE, {}) {
            val hook: HHOOK =
                SetWindowsHookExW(
                    WH_KEYBOARD_LL,
                    staticCFunction(::lowLevelKeyboardProc),
                    GetModuleHandleW(null),
                    0U
                ) ?: error("Unable to set native hook. Error code: ${GetLastError()}")

            memScoped {
                val msg = alloc<MSG>().ptr
                while (GetMessageW(msg, null, 0, 0) != 0) {
                    TranslateMessage(msg)
                    DispatchMessageA(msg)
                }
            }

            UnhookWindowsHookEx(hook)
        }
    }

    private fun stopReadingEvents() {
        PostThreadMessageW(threadId, WM_QUIT, 0, 0L)
    }

    /**
     * Processes the event.
     */
    internal fun process(isPressed: Boolean, vk: Int, scanCode: Int, extended: Boolean) {
        var keyCode = scanCode
        if (extended) {
            when (scanCode) {
                Key.LeftAlt.keyCode -> keyCode = Key.RightAlt.keyCode
                Key.LeftCtrl.keyCode -> keyCode = Key.RightCtrl.keyCode
            }
        }

        handlers.access {
            for ((id, handler) in it) {
                handler.handle(id, WINDOWS_VK_MAPPING[vk]?.keyCode ?: keyCode, isPressed)
            }
        }
    }
}

/**
 * Receives the event from the Windows MessageQueue and passes it to [NativeKeyboard].
 * This function is extracted out because of limitation of the Win32 API that it can only accept static functions.
 */
@ExperimentalJsExport
@ExperimentalUnsignedTypes
@ExperimentalKeyIO
internal fun lowLevelKeyboardProc(nCode: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
    try {
        val keyInfo = lParam.toCPointer<tagKBDLLHOOKSTRUCT>()!!.pointed
        val vk = keyInfo.vkCode.toInt()

        if (vk != VK_PACKET && keyInfo.flags.toInt() and NativeKeyboard.FAKE_ALT != NativeKeyboard.FAKE_ALT) {
            val keyEventType = when (wParam.toInt()) {
                WM_KEYDOWN, WM_SYSKEYDOWN -> true
                else -> false
            }

            val extended = keyInfo.flags.toInt() and 1 == 1
            NativeKeyboard.process(keyEventType, vk, keyInfo.scanCode.toInt(), extended)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return CallNextHookEx(null, nCode, wParam, lParam)
}

@ExperimentalTime
internal actual fun callAfter(duration: Duration, callback: () -> Unit) {
    Sleep(duration.toInt(DurationUnit.MILLISECONDS).toUInt())
    callback()
}

public actual typealias AtomicInt = kotlin.native.concurrent.AtomicInt
