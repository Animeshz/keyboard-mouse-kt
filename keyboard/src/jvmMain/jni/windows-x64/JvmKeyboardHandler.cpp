#include <stdio.h>
#include <windows.h>
#include <winuser.h>

#include <string>

#include "com_github_animeshz_keyboard_JvmKeyboardHandler.h"

#ifdef __cplusplus
extern "C" {
#endif

#define FAKE_ALT LLKHF_INJECTED | 0x20

DWORD threadId = 0;
JavaVM *jvm = NULL;
jobject JvmKeyboardHandler = NULL;
jmethodID emitEvent = NULL;

LRESULT CALLBACK LowLevelKeyboardProc(_In_ int nCode, _In_ WPARAM wParam, _In_ LPARAM lParam) {
    tagKBDLLHOOKSTRUCT *keyInfo = (tagKBDLLHOOKSTRUCT *)lParam;
    jint vk = keyInfo->vkCode;

    if (vk != VK_PACKET && keyInfo->flags & FAKE_ALT != FAKE_ALT) {
        jboolean isPressed = wParam == WM_KEYDOWN || wParam == WM_SYSKEYDOWN;
        jboolean extended = keyInfo->flags and 1;

        JNIEnv *env;
        if (jvm->AttachCurrentThread((void **)&env, NULL) >= JNI_OK) {
            jint scanCode = keyInfo->scanCode;
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

            env->CallVoidMethod(JvmKeyboardHandler, emitEvent, scanCode, isPressed);
        }
    }

    return CallNextHookEx(NULL, nCode, wParam, lParam);
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isCapsLockOn(JNIEnv *env, jobject obj) { return GetKeyState(0x14) & 1; }

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isNumLockOn(JNIEnv *env, jobject obj) { return GetKeyState(0x90) & 1; }

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isScrollLockOn(JNIEnv *env, jobject obj) { return GetKeyState(0x91) & 1; }

JNIEXPORT void JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeSendEvent(JNIEnv *env, jobject obj, jint scanCode, jboolean isDown) {
    INPUT input;
    input.type = INPUT_KEYBOARD;
    input.ki.time = 0;
    input.ki.dwExtraInfo = 0;

    // Send Windows/Super key with virtual code, because there's no particular scan code for that.
    if (scanCode == 125) {
        input.ki.wVk = 0x5B;
        input.ki.dwFlags = !isDown ? 2 : 0;
    } else {
        input.ki.wScan = scanCode;
        input.ki.dwFlags = 8U | (!isDown ? 2 : 0);
    }

    SendInput(1, &input, sizeof(input));
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeIsPressed(JNIEnv *env, jobject obj, jint scanCode) {
    int vk;
    if (scanCode == 125)
        vk = 0x5B;
    else
        vk = MapVirtualKeyA(scanCode, MAPVK_VSC_TO_VK_EX);

    return GetKeyState(vk) < 0;
}

JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeStartReadingEvents(JNIEnv *env, jobject obj) {
    HHOOK hook = SetWindowsHookExW(WH_KEYBOARD_LL, LowLevelKeyboardProc, GetModuleHandleW(NULL), 0);
    if (hook == NULL) return GetLastError();

    env->GetJavaVM(&jvm);
    threadId = GetCurrentThreadId();
    JvmKeyboardHandler = env->NewGlobalRef(obj);
    emitEvent = env->GetMethodID(env->GetObjectClass(obj), "emitEvent", "(IZ)V");

    MSG msg;
    while (GetMessageW(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessageA(&msg);
    }

    UnhookWindowsHookEx(hook);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeStopReadingEvents(JNIEnv *env, jobject obj) {
    if (JvmKeyboardHandler != NULL) {
        PostThreadMessage(threadId, WM_QUIT, 0, 0L);
        emitEvent = NULL;
        jvm = NULL;
        env->DeleteGlobalRef(JvmKeyboardHandler);
        JvmKeyboardHandler = NULL;
    }

    return 0;
}

#ifdef __cplusplus
}
#endif
