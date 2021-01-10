#include "X11KeyboardHandler.cpp"
#include "com_github_animeshz_keyboard_JvmKeyboardHandler.h"

#ifdef __cplusplus
extern "C" {
#endif

BaseKeyboardHandler *handler = NULL;

JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeInit(JNIEnv *env, jobject obj) {
    handler = X11KeyboardHandler::Create();
    if (handler != NULL) return 0;
    
    // handler = DeviceKeyboardHandler.Create();
    // if (handler != NULL) return 0;
    
    return 1;
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isCapsLockOn(JNIEnv *env, jobject obj) {
    return handler->isCapsLockOn();
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isNumLockOn(JNIEnv *env, jobject obj) {
    return handler->isNumLockOn();
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isScrollLockOn(JNIEnv *env, jobject obj) { return 0; }

JNIEXPORT void JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeSendEvent(JNIEnv *env, jobject obj, jint scanCode, jboolean isPressed) {
    return handler->sendEvent(scanCode, isPressed);
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeIsPressed(JNIEnv *env, jobject obj, jint scanCode) {
    return handler->isPressed(scanCode);
}

JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeStartReadingEvents(JNIEnv *env, jobject obj) {
    jmethodID emitEvent = env->GetMethodID(env->GetObjectClass(obj), "emitEvent", "(IZ)V");
    handler->startReadingEvents(env, obj, emitEvent);
    return 0;
}

JNIEXPORT void JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeStopReadingEvents(JNIEnv *env, jobject obj) {
    return handler->stopReadingEvents();
}

#ifdef __cplusplus
}
#endif
