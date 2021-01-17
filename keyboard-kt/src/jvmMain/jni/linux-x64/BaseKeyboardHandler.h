#include <jni.h>

class BaseKeyboardHandler {
   public:
    virtual bool isCapsLockOn() = 0;

    virtual bool isNumLockOn() = 0;

    virtual void sendEvent(int scanCode, bool isPressed) = 0;

    virtual bool isPressed(int scanCode) = 0;

    virtual void startReadingEvents(JNIEnv *env, jobject obj, jmethodID emitEvent) = 0;

    virtual void stopReadingEvents() = 0;
};