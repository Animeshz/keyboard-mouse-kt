#include <windows.h>
#include <winuser.h>

#include "../BaseKeyboardHandler.h"

#define FAKE_ALT LLKHF_INJECTED | 0x20

BaseKeyboardHandler *instance;
void (*callback)(int, bool);

class WindowsKeyboardHandler : BaseKeyboardHandler {
   private:
    DWORD threadId = 0;
    CRITICAL_SECTION cs;
    CONDITION_VARIABLE cv;

    WindowsKeyboardHandler() {}

   public:
    static BaseKeyboardHandler *getInstance() {
        if (!::instance) ::instance = new WindowsKeyboardHandler();
        return ::instance;
    }

    ~WindowsKeyboardHandler() { stopReadingEvents(); }

    bool isCapsLockOn() { return GetKeyState(0x14) & 1; }

    bool isNumLockOn() { return GetKeyState(0x90) & 1; }

    bool isScrollLockOn() { return GetKeyState(0x91) & 1; }

    void sendEvent(int scanCode, bool isPressed) {
        INPUT input;
        input.type = INPUT_KEYBOARD;
        input.ki.time = 0;
        input.ki.dwExtraInfo = 0;

        // Send Windows/Super key with virtual code, because there's no particular scan code for that.
        int extended = 0;
        switch (scanCode) {
            case 54:
            case 97:
            case 100:
            case 126:
                extended = 1;
                break;
        }

        if (scanCode == 125) {
            input.ki.wVk = 0x5B;
            input.ki.dwFlags = (isPressed ? 0 : 2) | extended;
        } else {
            input.ki.wScan = scanCode;
            input.ki.dwFlags = 8U | (isPressed ? 0 : 2) | extended;
        }

        SendInput(1, &input, sizeof(INPUT));
    }

    bool isPressed(int scanCode) {
        int vk;
        if (scanCode == 125)
            vk = 0x5B;
        else
            vk = MapVirtualKeyA(scanCode, MAPVK_VSC_TO_VK_EX);

        return GetKeyState(vk) < 0;
    }

    int startReadingEvents(void (*callback)(int, bool)) {
        int ret = 0;
        ::callback = callback;

        InitializeCriticalSection (&cs);
        InitializeConditionVariable (&cv);

        EnterCriticalSection(&cs);
        CreateThread(NULL, 0, readInThread, (LPVOID)&ret, 0, &threadId);
        SleepConditionVariableCS(&cv, &cs, INFINITE);
        LeaveCriticalSection(&cs);

        return ret;
    }

    void stopReadingEvents() { PostThreadMessage(threadId, WM_QUIT, 0, 0L); }

    static DWORD WINAPI readInThread(LPVOID data) {
        HHOOK hook = SetWindowsHookExW(WH_KEYBOARD_LL, LowLevelKeyboardProc, GetModuleHandleW(NULL), 0);
        if (hook == NULL) {
            *(int *)data = GetLastError();
        }
        data = NULL;  // Immediately remove pointer to the stack variable

        auto handler = (WindowsKeyboardHandler *)WindowsKeyboardHandler::getInstance();
//        EnterCriticalSection(&handler->cs);
//        WakeConditionVariable(&handler->cv);
//        LeaveCriticalSection(&handler->cs);

        MSG msg;
        while (GetMessageW(&msg, NULL, 0, 0)) {
            TranslateMessage(&msg);
            DispatchMessageA(&msg);
        }

        UnhookWindowsHookEx(hook);
        return 0;
    }

    static LRESULT CALLBACK LowLevelKeyboardProc(int nCode, WPARAM wParam, LPARAM lParam) {
        tagKBDLLHOOKSTRUCT *keyInfo = (tagKBDLLHOOKSTRUCT *)lParam;
        int vk = keyInfo->vkCode;

        if (vk != VK_PACKET && keyInfo->flags & FAKE_ALT != FAKE_ALT) {
            bool isPressed = wParam == WM_KEYDOWN || wParam == WM_SYSKEYDOWN;
            bool extended = keyInfo->flags & 1;

            int scanCode = keyInfo->scanCode;
            switch (vk) {
                case 0x21:
                    scanCode = 104;
                    break;
                case 0x22:
                    scanCode = 109;
                    break;
                case 0x23:
                    scanCode = 107;
                    break;
                case 0x24:
                    scanCode = 102;
                    break;
                case 0x25:
                    scanCode = 105;
                    break;
                case 0x26:
                    scanCode = 103;
                    break;
                case 0x27:
                    scanCode = 106;
                    break;
                case 0x28:
                    scanCode = 108;
                    break;
                case 0x5B:
                    scanCode = 125;
                    break;
            }

            if (extended) {
                if (scanCode == 56) {
                    scanCode = 100;
                } else if (scanCode == 29) {
                    scanCode = 97;
                }
            }

            ::callback(scanCode, isPressed);
        }

        return CallNextHookEx(NULL, nCode, wParam, lParam);
    }
};
