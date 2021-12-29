#include "X11Keyboard.cpp"

namespace Keyboard {

enum HandlerType { X11, DEVICE, UNINITIALIZED, NONE };
HandlerType type = UNINITIALIZED;

KeyboardMouseInternal *getInstance() {
    switch (type) {
        case NONE:
            return NULL;

        case X11:
            return X11Keyboard::getInstance();

        case DEVICE:
            return NULL;

        case UNINITIALIZED:
            KeyboardMouseInternal *handler;
            if ((handler = X11Keyboard::getInstance()) != NULL) {
                type = X11;
                return handler;
            }

            if (false /* Device */) {
                type = DEVICE;
                return NULL;
            }

            type = NONE;
            return NULL;

        default:
            return NULL;  // Never reached
    }
}

}  // namespace Keyboard
