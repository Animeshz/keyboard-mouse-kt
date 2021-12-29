#include "exports.h"
#include "KeyboardMouseInternal.hpp"

void init() {
    getInstance();
}

void register_evlis(void (*fn)(int, bool)) {
    getInstance()->startReadingEvents(fn);
}

void unregister_evlis() {
    getInstance()->stopReadingEvents();
}

void sendEvent(int keyCode, bool isPressed) {
    getInstance()->sendEvent(keyCode, isPressed);
}

bool isPressed(int keyCode) {
    return getInstance()->isPressed(keyCode);
}

bool isCapsLockOn() {
    return getInstance()->isCapsLockOn();
}
bool isNumLockOn() {
    return getInstance()->isNumLockOn();
}
bool isScrollLockOn() {
    return getInstance()->isScrollLockOn();
}


