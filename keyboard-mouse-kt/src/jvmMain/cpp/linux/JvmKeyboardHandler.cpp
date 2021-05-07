#include "LinuxKeyboardHandler.cpp"
#include "io_github_animeshz_keyboard_NativeKeyboard.h"

#ifdef __cplusplus
extern "C" {
#endif

JavaVM *jvm = NULL;
jobject JvmKeyboardHandler = NULL;
jmethodID emitEvent = NULL;

JNIEXPORT jint JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeInit(JNIEnv *env, jobject obj) {
    if (LinuxKeyboardHandler::getInstance() != NULL) return 0;

    return 1;
}

JNIEXPORT jboolean JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_isCapsLockOn(JNIEnv *env, jobject obj) { return LinuxKeyboardHandler::getInstance()->isCapsLockOn(); }

JNIEXPORT jboolean JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_isNumLockOn(JNIEnv *env, jobject obj) { return LinuxKeyboardHandler::getInstance()->isNumLockOn(); }

JNIEXPORT jboolean JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_isScrollLockOn(JNIEnv *env, jobject obj) { return 0; }

JNIEXPORT void JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeSendEvent(JNIEnv *env, jobject obj, jint scanCode, jboolean isPressed) {
    return LinuxKeyboardHandler::getInstance()->sendEvent(scanCode, isPressed);
}

JNIEXPORT jboolean JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeIsPressed(JNIEnv *env, jobject obj, jint scanCode) { return LinuxKeyboardHandler::getInstance()->isPressed(scanCode); }

void emitEventToJvm(int scanCode, bool isPressed) {
    JNIEnv *newEnv;
    if (jvm->AttachCurrentThread((void **)&newEnv, NULL) >= JNI_OK) {
        newEnv->CallVoidMethod(JvmKeyboardHandler, emitEvent, scanCode, isPressed);
    }
}

JNIEXPORT jint JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeStartReadingEvents(JNIEnv *env, jobject obj) {
    env->GetJavaVM(&jvm);
    JvmKeyboardHandler = env->NewGlobalRef(obj);
    emitEvent = env->GetMethodID(env->GetObjectClass(obj), "emitEvent", "(IZ)V");

    return LinuxKeyboardHandler::getInstance()->startReadingEvents(emitEventToJvm);
}

JNIEXPORT void JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeStopReadingEvents(JNIEnv *env, jobject obj) {
    return LinuxKeyboardHandler::getInstance()->stopReadingEvents();

    env->DeleteGlobalRef(JvmKeyboardHandler);
    JvmKeyboardHandler = NULL;
    jvm = NULL;
}

#ifdef __cplusplus
}
#endif
