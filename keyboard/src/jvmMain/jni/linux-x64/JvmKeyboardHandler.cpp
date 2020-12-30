#include "com_github_animeshz_keyboard_JvmKeyboardHandler.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isCapsLockOn
  (JNIEnv *env, jobject obj) {
  return 1;
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isNumLockOn
  (JNIEnv *env, jobject obj) {
  return 1;
}

JNIEXPORT jboolean JNICALL Java_com_github_animeshz_keyboard_JvmKeyboardHandler_isScrollLockOn
  (JNIEnv *env, jobject obj) {
  return 1;
}

#ifdef __cplusplus
}
#endif
