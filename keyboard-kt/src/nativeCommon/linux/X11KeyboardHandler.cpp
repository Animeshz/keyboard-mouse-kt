#include <X11/Xlib.h>
#include <X11/extensions/XInput2.h>
#include <X11/extensions/XTest.h>
#include <dlfcn.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <functional>
#include <future>
#include <thread>

#include "../BaseKeyboardHandler.h"

class X11KeyboardHandler : BaseKeyboardHandler {
   private:
    void *x11;
    void *xInput2;
    void *xTest;
    Display *display;

    std::condition_variable cv;
    std::mutex cv_m;
    bool stopReading;

    static X11KeyboardHandler *instance;
    void (*callback)(int, bool);

    X11KeyboardHandler(void *x11, void *xInput2, void *xTest,
                       Display *display) {
        this->x11 = x11;
        this->xInput2 = xInput2;
        this->xTest = xTest;
        this->display = display;
    }

    inline int toggleStates() {
        XKeyboardState mask;
        XGetKeyboardControl(display, &mask);
        return mask.led_mask;
    }

    static void Create() {
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
        if (dlsym(xInput2, "XISelectEvents") == NULL) {
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

        std::promise<Display *> p;
        auto f = p.get_future();
        std::thread t(setupDisplayAndReadWhenRequired, std::move(p));
        t.detach();
        Display *display = f.get();

        if (display == NULL) {
            dlclose(x11);
            dlclose(xInput2);
            dlclose(xTest);
            return NULL;
        }

        instance = new X11KeyboardHandler(x11, xInput2, xTest, display);
        
        // Wait till we've done setting up the masks
        std::lock_guard<std::mutex> lk(instance->cv_m);
        instance->cv.wait(lk);
    }

    static void setupDisplayAndReadWhenRequired(std::promise<Display *> &&p) {
        Display *display = XOpenDisplay(NULL);
        p.set_value(display);

        if (display == NULL) {
            return;
        }

        int xiOpcode;

        // Discard single use variables from stack as we don't need them
        {
            int queryEvent;
            int queryError;
            XQueryExtension(display, "XInputExtension", &xiOpcode, &queryEvent,
                            &queryError);

            Window root = XDefaultRootWindow(display);
            int maskLen = XIMaskLen(XI_LASTEVENT);
            unsigned char mask[maskLen];

            XIEventMask xiMask;
            xiMask->deviceid = XIAllMasterDevices;
            xiMask->mask_len = maskLen;
            xiMask->mask = mask;

            XISetMask(xiMask->mask, XI_RawKeyPress);
            XISetMask(xiMask->mask, XI_RawKeyRelease);
            XISelectEvents(display, root, xiMask, 1);
            XSync(display, 0);
        }

        X11KeyboardHandler *handler;
        while ((handler = instance) == NULL) {
            // We shouldn't fall here, but who knows?
            usleep(20);
        }

        // Notify we've set up masking, so the handler becomes usable.
        handler->cv.notify_all();

        std::unique_lock<std::mutex> lk(handler->cv_m);
        while (true) {
            // Wait till we asked to collect the events
            handler->cv.wait(lk);

            handler->stopReading = false;

            // Clear old events in the queue.
            XSync(display, 1);
            XEvent event;

            while (true) {
                XNextEvent(display, &event);
                if (handler->stopReading) break;

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
                    handler->callback(cookieData->detail - 8, keyEventType);
                }

                XFreeEventData(display, &cookie);
            }
        }
    }

   public:
    static BaseKeyboardHandler *getInstance() {
        if (!instance) Create();

        return instance;
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

    int startReadingEvents(void (*callback)(int, bool)) {
        {
            std::lock_guard<std::mutex> lk(cv_m);
            this->callback = callback;
        }
        cv.notify_all();

        return 0;
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

X11KeyboardHandler *X11KeyboardHandler::instance = NULL;