#include "X11KeyboardHandler.cpp"

namespace LinuxKeyboardHandler {

enum HandlerType { X11, DEVICE, UNINITIALIZED, NONE };

HandlerType type = UNINITIALIZED;

BaseKeyboardHandler *getInstance() {
    switch (type) {
        case NONE:
            return NULL;

        case X11:
            return X11KeyboardHandler::getInstance();

        case DEVICE:
            return NULL;

        case UNINITIALIZED:
            BaseKeyboardHandler *handler;
            if ((handler = X11KeyboardHandler::getInstance()) != NULL) {
                type = X11;
                return handler;
            }

            if (false /* Device */) {
                type = DEVICE;
                return NULL;
            }

            type = NONE;
            return NULL;
    }
}

}  // namespace LinuxKeyboardHandler