#include "Keyboard.cpp"
#include "io_github_animeshz_keyboard_NativeKeyboard.h"

#ifdef __cplusplus
extern "C" {
#endif

JavaVM *jvm = NULL;
jobject JvmKeyboard = NULL;
jmethodID emitEvent = NULL;

JNIEXPORT jint JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeInit(JNIEnv *env, jobject obj) {
    if (Keyboard::getInstance() != NULL) return 0;

    return 1;
}

JNIEXPORT jboolean JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_isCapsLockOn(JNIEnv *env, jobject obj) { return Keyboard::getInstance()->isCapsLockOn(); }

JNIEXPORT jboolean JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_isNumLockOn(JNIEnv *env, jobject obj) { return Keyboard::getInstance()->isNumLockOn(); }

JNIEXPORT jboolean JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_isScrollLockOn(JNIEnv *env, jobject obj) { return 0; }

JNIEXPORT void JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeSendEvent(JNIEnv *env, jobject obj, jint scanCode, jboolean isPressed) {
    return Keyboard::getInstance()->sendEvent(scanCode, isPressed);
}

JNIEXPORT jboolean JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeIsPressed(JNIEnv *env, jobject obj, jint scanCode) { return Keyboard::getInstance()->isPressed(scanCode); }

void emitEventToJvm(int scanCode, bool isPressed) {
    JNIEnv *newEnv;
    if (jvm->AttachCurrentThread((void **)&newEnv, NULL) >= JNI_OK) {
        newEnv->CallVoidMethod(JvmKeyboard, emitEvent, scanCode, isPressed);
    }
}

JNIEXPORT jint JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeStartReadingEvents(JNIEnv *env, jobject obj) {
    env->GetJavaVM(&jvm);
    JvmKeyboard = env->NewGlobalRef(obj);
    emitEvent = env->GetMethodID(env->GetObjectClass(obj), "emitEvent", "(IZ)V");

    return Keyboard::getInstance()->startReadingEvents(emitEventToJvm);
}

JNIEXPORT void JNICALL Java_io_github_animeshz_keyboard_NativeKeyboard_nativeStopReadingEvents(JNIEnv *env, jobject obj) {
    return Keyboard::getInstance()->stopReadingEvents();

    env->DeleteGlobalRef(JvmKeyboard);
    JvmKeyboard = NULL;
    jvm = NULL;
}

#ifdef __cplusplus
}
#endif
