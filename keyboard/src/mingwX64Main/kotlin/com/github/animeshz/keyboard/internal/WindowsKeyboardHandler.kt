package com.github.animeshz.keyboard.internal

import com.github.animeshz.keyboard.ExperimentalKeyIO
import com.github.animeshz.keyboard.entity.Key
import com.github.animeshz.keyboard.events.KeyEvent
import com.github.animeshz.keyboard.events.KeyEventType
import kotlin.native.concurrent.AtomicNativePtr
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.internal.NativePtr
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toCPointer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import platform.windows.CallNextHookEx
import platform.windows.DispatchMessageA
import platform.windows.GetLastError
import platform.windows.GetMessageW
import platform.windows.GetModuleHandleW
import platform.windows.INPUT
import platform.windows.LLKHF_INJECTED
import platform.windows.LPARAM
import platform.windows.LRESULT
import platform.windows.MSG
import platform.windows.SendInput
import platform.windows.SetWindowsHookExW
import platform.windows.TranslateMessage
import platform.windows.UnhookWindowsHookEx
import platform.windows.VK_PACKET
import platform.windows.WH_KEYBOARD_LL
import platform.windows.WM_KEYDOWN
import platform.windows.WM_SYSKEYDOWN
import platform.windows.WPARAM
import platform.windows.tagKBDLLHOOKSTRUCT

@ExperimentalKeyIO
@ExperimentalUnsignedTypes
public object WindowsKeyboardHandler : NativeKeyboardHandler {
    private val worker = Worker.start(errorReporting = true, name = "WindowsKeyboardHandler")
    private val hook: AtomicNativePtr = AtomicNativePtr(NativePtr.NULL)

    private val ignoreNextRightAlt: AtomicBoolean = atomic(false)

    private val eventsInternal = MutableSharedFlow<KeyEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<KeyEvent> = eventsInternal.asSharedFlow()

    init {
        // When subscriptionCount increments from 0 to 1, start the native hook.
        eventsInternal.subscriptionCount
                .filter { it > 0 }
                .distinctUntilChanged()
                .onEach {
                    worker.execute(mode = TransferMode.SAFE, { this }) { handler ->
                        prepare()
                        startMessagePumping()
                        cleanup()
                    }
                }
                .launchIn(CoroutineScope(Dispatchers.Unconfined))
    }

    // TODO("Add support for extended key sending")
    override fun sendEvent(keyEvent: KeyEvent) {
        return memScoped {
            val input = alloc<INPUT>().apply {
                type = INPUT_KEYBOARD
                ki.wVk = keyEvent.key.keyCode.toUShort()
                ki.dwFlags = if (keyEvent.type == KeyEventType.KeyDown) 0U else 2U
                ki.time = 0U
                ki.dwExtraInfo = 0U
            }

            SendInput(1, input.ptr, sizeOf<INPUT>().toInt())
        }
    }

    // ==================================== Internals ====================================
    public const val FAKE_ALT: Int = LLKHF_INJECTED or 0x20
    private const val INPUT_KEYBOARD = 1U

    /**
     * Registers the native hook.
     */
    private fun prepare() {
        hook.value = SetWindowsHookExW(
                WH_KEYBOARD_LL,
                staticCFunction(::lowLevelKeyboardProc),
                GetModuleHandleW(null),
                0U
        )?.rawValue ?: throw RuntimeException("Unable to set native hook, Report it with error code: ${GetLastError()}")
    }

    /**
     * Polls the message queue in Windows to receive events in [lowLevelKeyboardProc].
     * When [MutableSharedFlow.subscriptionCount] of [eventsInternal] reduces to 0, breaks the procedure and exits.
     */
    private fun startMessagePumping() {
        memScoped {
            val msg = alloc<MSG>().ptr
            while (GetMessageW(msg, null, 0, 0) != 0) {
                TranslateMessage(msg)
                DispatchMessageA(msg)
                if (eventsInternal.subscriptionCount.value == 0) break
            }
        }
    }

    /**
     * Cleans up the handler.
     */
    private fun cleanup() {
        val hookPtr = hook.value
        if (hookPtr != NativePtr.NULL) {
            UnhookWindowsHookEx(interpretCPointer(hookPtr))
            hook.value = NativePtr.NULL
        }
        ignoreNextRightAlt.value = false
    }

    /**
     * Processes the event.
     */
    // TODO("Add support for extended key parsing")
    internal fun process(keyEventType: KeyEventType, vk: Int, scanCode: Int, extended: Boolean) {
        if (vk == 0xA5 && ignoreNextRightAlt.getAndSet(false)) return
        if (scanCode == 541 && vk == 162) ignoreNextRightAlt.value = true

        val key = Key.fromKeyCode(vk)
        eventsInternal.tryEmit(KeyEvent(key, keyEventType))
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
                WM_KEYDOWN, WM_SYSKEYDOWN -> KeyEventType.KeyDown
                else -> KeyEventType.KeyUp
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
public actual fun nativeKbHandlerForPlatform(): NativeKeyboardHandler {
    return WindowsKeyboardHandler
}