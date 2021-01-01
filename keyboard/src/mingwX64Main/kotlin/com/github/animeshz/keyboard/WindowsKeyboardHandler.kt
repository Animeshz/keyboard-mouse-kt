package com.github.animeshz.keyboard

import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyState
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toCPointer
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
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
import platform.windows.TranslateMessage
import platform.windows.UnhookWindowsHookEx
import platform.windows.VK_PACKET
import platform.windows.WH_KEYBOARD_LL
import platform.windows.WM_KEYDOWN
import platform.windows.WM_QUIT
import platform.windows.WM_SYSKEYDOWN
import platform.windows.WPARAM
import platform.windows.tagKBDLLHOOKSTRUCT
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker

@ExperimentalKeyIO
@ExperimentalUnsignedTypes
internal object WindowsKeyboardHandler : NativeKeyboardHandlerBase() {
    override fun sendEvent(keyEvent: KeyEvent) {
        if (keyEvent.key == Key.Unknown) return

        memScoped {
            val input = alloc<INPUT>().apply {
                type = INPUT_KEYBOARD
                ki.time = 0U
                ki.dwExtraInfo = 0U

                // Send Windows/Super key with virtual code, because there's no particular scan code for that.
                if (keyEvent.key == Key.LeftSuper) {
                    ki.wVk = 0x5B.toUShort()
                    ki.dwFlags = if (keyEvent.state == KeyState.KeyUp) 2U else 0U
                } else {
                    ki.wScan = keyEvent.key.keyCode.toUShort()
                    ki.dwFlags = 8U or if (keyEvent.state == KeyState.KeyUp) 2U else 0U
                }
            }

            SendInput(1, input.ptr, sizeOf<INPUT>().toInt())
        }
    }

    override fun getKeyState(key: Key): KeyState {
        if (key == Key.Unknown) return KeyState.KeyUp

        val vk = if (key == Key.LeftSuper) 0x5B
        else MapVirtualKeyA(key.keyCode.toUInt(), MAPVK_VSC_TO_VK_EX).toInt()

        return if (GetKeyState(vk) < 0) KeyState.KeyDown else KeyState.KeyUp
    }

    override fun isCapsLockOn(): Boolean = GetKeyState(0x14).toInt() and 1 != 0

    override fun isNumLockOn(): Boolean = GetKeyState(0x90).toInt() and 1 != 0

    override fun isScrollLockOn(): Boolean = GetKeyState(0x91).toInt() and 1 != 0

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

    private val worker = Worker.start(errorReporting = true, name = "WindowsKeyboardHandler")
    private val threadId = worker.execute(mode = TransferMode.SAFE, {}) { GetCurrentThreadId() }.result

    init {
        // Force execute cleanup handlers on SIGINT (Ctrl + C)
        signal(SIGINT, staticCFunction { _ -> exit(0) })

        atexit(
            staticCFunction { ->
                unconfinedScope.cancel()
                stopReadingEvents()
                worker.requestTermination(true).consume {}
            }
        )
    }

    /**
     * Polls the message queue in Windows to receive events in [lowLevelKeyboardProc].
     * When [MutableSharedFlow.subscriptionCount] of [eventsInternal] reduces to 0, breaks the procedure and exits.
     */
    override fun startReadingEvents() {
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

    override fun stopReadingEvents() {
        PostThreadMessageW(threadId, WM_QUIT, 0, 0L)
    }

    /**
     * Processes the event.
     */
    internal fun process(keyState: KeyState, vk: Int, scanCode: Int, extended: Boolean) {
        var keyCode = scanCode
        if (extended) {
            when (scanCode) {
                Key.LeftAlt.keyCode -> keyCode = Key.RightAlt.keyCode
                Key.LeftCtrl.keyCode -> keyCode = Key.RightCtrl.keyCode
            }
        }

        val key = WINDOWS_VK_MAPPING[vk] ?: Key.fromKeyCode(keyCode)
        eventsInternal.tryEmit(KeyEvent(key, keyState))
    }
}

/**
 * Receives the event from the Windows MessageQueue and passes it to [WindowsKeyboardHandler].
 * This function is extracted out because of limitation of the Win32 API that it can only accept static functions.
 */
@ExperimentalUnsignedTypes
@ExperimentalKeyIO
internal fun lowLevelKeyboardProc(nCode: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
    try {
        val keyInfo = lParam.toCPointer<tagKBDLLHOOKSTRUCT>()!!.pointed
        val vk = keyInfo.vkCode.toInt()

        if (vk != VK_PACKET && keyInfo.flags.toInt() and WindowsKeyboardHandler.FAKE_ALT != WindowsKeyboardHandler.FAKE_ALT) {
            val keyEventType = when (wParam.toInt()) {
                WM_KEYDOWN, WM_SYSKEYDOWN -> KeyState.KeyDown
                else -> KeyState.KeyUp
            }

            val extended = keyInfo.flags.toInt() and 1 == 1
            WindowsKeyboardHandler.process(keyEventType, vk, keyInfo.scanCode.toInt(), extended)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return CallNextHookEx(null, nCode, wParam, lParam)
}

/**
 * Gets the [NativeKeyboardHandler] for Windows platform.
 */
@ExperimentalUnsignedTypes
@ExperimentalKeyIO
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler = WindowsKeyboardHandler
