#include <stdio.h>

#include <string>

#include "WindowsKeybaordHandler.cpp"
#include "com_github_animeshz_keyboard_JvmKeyboardHandler.h"

#ifdef __cplusplus
extern "C" {
#endif

#define FAKE_ALT LLKHF_INJECTED | 0x20

JavaVM *jvm = NULL;
jobject JvmKeyboardHandler = NULL;

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isCapsLockOn(JNIEnv *env, jobject obj) { return WindowsKeyboardHandler::getInstance()->isCapsLockOn(); }

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isNumLockOn(JNIEnv *env, jobject obj) { return WindowsKeyboardHandler::getInstance()->isNumLockOn(); }

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isScrollLockOn(JNIEnv *env, jobject obj) { return WindowsKeyboardHandler::getInstance()->isScrollLockOn(); }

JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeInit(JNIEnv *env, jobject obj) { return 0; }

JNIEXPORT void JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeSendEvent(JNIEnv *env, jobject obj, jint scanCode, jboolean isDown) {
    WindowsKeyboardHandler::getInstance()->sendEvent(scanCode, isDown);
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeIsPressed(JNIEnv *env, jobject obj, jint scanCode) {
    return WindowsKeyboardHandler::getInstance()->isPressed(scanCode);
}

JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeStartReadingEvents(JNIEnv *env, jobject obj) {
    env->GetJavaVM(&jvm);
    JvmKeyboardHandler = env->NewGlobalRef(obj);
    jmethodID emitEvent = env->GetMethodID(env->GetObjectClass(obj), "emitEvent", "(IZ)V");
    
    return WindowsKeyboardHandler::getInstance()->startReadingEvents(
        [emitEvent](int scanCode, bool isPressed) {
            JNIEnv *newEnv;
            if (jvm->AttachCurrentThread((void **)&newEnv, NULL) >= JNI_OK && JvmKeyboardHandler != NULL) {
                newEnv->CallVoidMethod(JvmKeyboardHandler, emitEvent, scanCode, isPressed);
            }
        }
    );
}

JNIEXPORT void JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeStopReadingEvents(JNIEnv *env, jobject obj) {
    if (JvmKeyboardHandler == NULL) return;

    WindowsKeyboardHandler::getInstance()->stopReadingEvents();

    env->DeleteGlobalRef(JvmKeyboardHandler);
    JvmKeyboardHandler = NULL;
    jvm = NULL;
}

#ifdef __cplusplus
}
#endif
