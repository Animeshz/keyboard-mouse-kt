#include "com_github_animeshz_keyboard_jni_Test.h"

JNIEXPORT jint JNICALL Java_com_github_animeshz_keyboard_jni_Test_test
  (JNIEnv *env, jobject obj, jint i) {
    return i+1;
}
