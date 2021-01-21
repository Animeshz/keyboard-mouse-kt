#include <X11/Xlib.h>
#include <X11/extensions/XInput2.h>
#include <X11/extensions/XTest.h>
#include <dlfcn.h>
#include <stdlib.h>
#include <string.h>

#include <functional>
#include <thread>

#include "../BaseKeyboardHandler.h"

class X11KeyboardHandler : BaseKeyboardHandler {
   private:
    void *x11;
    void *xInput2;
    void *xTest;
    Display *display;
    int xiOpcode;
    volatile bool stopReading = false;

    X11KeyboardHandler(void *x11, void *xInput2, void *xTest, Display *display,
                       int xiOpcode) {
        this->x11 = x11;
        this->xInput2 = xInput2;
        this->xTest = xTest;
        this->display = display;
        this->xiOpcode = xiOpcode;
    }

    inline int toggleStates() {
        XKeyboardState mask;
        XGetKeyboardControl(display, &mask);
        return mask.led_mask;
    }

   public:
    static BaseKeyboardHandler *Create() {
        if (getenv("DISPLAY") == NULL) {
            return NULL;
        }

        void *x11 = dlopen("libX11.so.6", RTLD_GLOBAL | RTLD_LAZY);
        if (x11 == NULL) {
            return NULL;
        }

        void *xInput2 = dlopen("libXi.so.6", RTLD_GLOBAL | RTLD_LAZY);
        if (xInput2 == NULL) {
            dlclose(x11);
            return NULL;
        }

        void *xTest = dlopen("libXtst.so.6", RTLD_GLOBAL | RTLD_LAZY);
        if (xTest == NULL) {
            dlclose(x11);
            dlclose(xInput2);
            return NULL;
        }

        // Check XInput2 functions are present, since libXi may contain XInput
        // or XInput2.
        void *f = dlsym(xInput2, "XISelectEvents");
        if (f == NULL) {
            dlclose(x11);
            dlclose(xInput2);
            dlclose(xTest);
            return NULL;
        }

        // Load definitions
        dlsym(x11, "XOpenDisplay");
        dlsym(x11, "XDefaultRootWindow");
        dlsym(x11, "XQueryExtension");
        dlsym(x11, "XFlush");

        dlsym(x11, "XSync");
        dlsym(x11, "XQueryKeymap");
        dlsym(x11, "XNextEvent");
        dlsym(x11, "XSendEvent");

        dlsym(x11, "XFreeEventData");
        dlsym(x11, "XGetEventData");
        dlsym(x11, "XGetInputFocus");
        dlsym(x11, "XGetKeyboardControl");
        dlsym(xInput2, "XISelectEvents");

        dlsym(xTest, "XTestFakeKeyEvent");

        Display *display = XOpenDisplay(NULL);
        if (display == NULL) {
            dlclose(x11);
            dlclose(xInput2);
            dlclose(xTest);
            return NULL;
        }

        int xiOpcode;
        int queryEvent;
        int queryError;
        XQueryExtension(display, "XInputExtension", &xiOpcode, &queryEvent,
                        &queryError);

        return new X11KeyboardHandler(x11, xInput2, xTest, display, xiOpcode);
    }

    ~X11KeyboardHandler() {
        stopReadingEvents();
        XCloseDisplay(display);
        dlclose(x11);
        dlclose(xInput2);
        dlclose(xTest);
    }

    bool isCapsLockOn() { return toggleStates() & 1; }

    bool isNumLockOn() { return toggleStates() & 2; }

    bool isScrollLockOn() { return 0; }

    void sendEvent(int scanCode, bool isPressed) {
        // https://stackoverflow.com/a/42020068/11377112
        XTestFakeKeyEvent(display, scanCode + 8, isPressed, 0);
        XFlush(display);
    }

    bool isPressed(int scanCode) {
        char keyStates[32];
        XQueryKeymap(display, keyStates);
        int xKeyCode = scanCode + 8;

        return keyStates[xKeyCode / 8] & (1 << (xKeyCode % 8));
    }

    int startReadingEvents(std::function<void(int, bool)> callback) {
        std::thread th(&X11KeyboardHandler::readInThread, this, callback);

        return 0;
    }

    void readInThread(std::function<void(int, bool)> callback) {
        Window root = XDefaultRootWindow(display);
        XIEventMask *xiMask = new XIEventMask;
        xiMask->deviceid = XIAllMasterDevices;
        xiMask->mask_len = XIMaskLen(XI_LASTEVENT);
        xiMask->mask = new unsigned char[xiMask->mask_len]();

        XISetMask(xiMask->mask, XI_RawKeyPress);
        XISetMask(xiMask->mask, XI_RawKeyRelease);
        XISelectEvents(display, root, xiMask, 1);
        XSync(display, 0);

        delete[] xiMask->mask;
        delete xiMask;

        stopReading = false;
        XEvent event;

        while (true) {
            XNextEvent(display, &event);
            if (stopReading) break;

            XGenericEventCookie cookie = event.xcookie;
            if (cookie.type != GenericEvent || cookie.extension != xiOpcode)
                continue;

            if (XGetEventData(display, &cookie)) {
                bool keyEventType;
                if (cookie.evtype == XI_RawKeyPress)
                    keyEventType = 1;
                else if (cookie.evtype == XI_RawKeyRelease)
                    keyEventType = 0;
                else
                    continue;

                XIRawEvent *cookieData = (XIRawEvent *)cookie.data;
                callback(cookieData->detail - 8, keyEventType);
            }

            XFreeEventData(display, &cookie);
        }
    }

    void stopReadingEvents() {
        stopReading = true;

        // Send dummy event, so that event loop exits
        XClientMessageEvent dummyEvent;
        memset(&dummyEvent, 0, sizeof(XClientMessageEvent));
        dummyEvent.type = 33;
        dummyEvent.format = 32;

        XSendEvent(display, XDefaultRootWindow(display), 0, 0,
                   (XEvent *)&dummyEvent);
        XFlush(display);
    }
};
