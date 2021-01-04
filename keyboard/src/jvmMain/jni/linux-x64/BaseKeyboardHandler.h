#include <jni.h>

class BaseKeyboardHandler {
   public:
    virtual bool isCapsLockOn();

    virtual bool isNumLockOn();

    virtual void sendEvent(int scanCode, bool isPressed);

    virtual bool isPressed(int scanCode);

    virtual void startReadingEvents(JNIEnv *env, jobject obj, jmethodID emitEvent);

    virtual void stopReadingEvents();
};