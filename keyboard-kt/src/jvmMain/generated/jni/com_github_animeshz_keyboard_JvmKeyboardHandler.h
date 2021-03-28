/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_github_animeshz_keyboard_JvmKeyboardHandler */

#ifndef _Included_com_github_animeshz_keyboard_JvmKeyboardHandler
#define _Included_com_github_animeshz_keyboard_JvmKeyboardHandler
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_github_animeshz_keyboard_JvmKeyboardHandler
 * Method:    isCapsLockOn
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isCapsLockOn
  (JNIEnv *, jobject);

/*
 * Class:     com_github_animeshz_keyboard_JvmKeyboardHandler
 * Method:    isNumLockOn
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isNumLockOn
  (JNIEnv *, jobject);

/*
 * Class:     com_github_animeshz_keyboard_JvmKeyboardHandler
 * Method:    isScrollLockOn
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isScrollLockOn
  (JNIEnv *, jobject);

/*
 * Class:     com_github_animeshz_keyboard_JvmKeyboardHandler
 * Method:    nativeInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeInit
  (JNIEnv *, jobject);

/*
 * Class:     com_github_animeshz_keyboard_JvmKeyboardHandler
 * Method:    nativeSendEvent
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeSendEvent
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     com_github_animeshz_keyboard_JvmKeyboardHandler
 * Method:    nativeIsPressed
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeIsPressed
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_github_animeshz_keyboard_JvmKeyboardHandler
 * Method:    nativeStartReadingEvents
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeStartReadingEvents
  (JNIEnv *, jobject);

/*
 * Class:     com_github_animeshz_keyboard_JvmKeyboardHandler
 * Method:    nativeStopReadingEvents
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_nativeStopReadingEvents
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif