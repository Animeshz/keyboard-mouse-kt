#include <functional>

class BaseKeyboardHandler {
   public:
    virtual bool isCapsLockOn() = 0;

    virtual bool isNumLockOn() = 0;

    virtual bool isScrollLockOn() = 0;

    virtual void sendEvent(int scanCode, bool isPressed) = 0;

    virtual bool isPressed(int scanCode) = 0;

    virtual int startReadingEvents(void (*callback)(int, bool)) = 0;

    virtual void stopReadingEvents() = 0;
};