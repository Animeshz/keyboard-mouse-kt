#pragma once

void init();
void register_evlis(void (*fn)(int, bool));
void unregister_evlis();
void sendEvent(int keyCode, bool isPressed);
bool isPressed(int keyCode);
bool isCapsLockOn();
bool isNumLockOn();
bool isScrollLockOn();
