#include <napi.h>

#include "WindowsKeyboardHandler.cpp"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct EventData {
    int scanCode;
    bool isPressed;
} EventData;

void EmitEventToJs(Napi::Env env, Napi::Function callback, std::nullptr_t* context, EventData* data);
using TSFN = Napi::TypedThreadSafeFunction<std::nullptr_t, EventData, EmitEventToJs>;

TSFN ts_callback;

// No checks because these are part of internal API.

void Send(const Napi::CallbackInfo& info) {
    int scanCode = info[0].As<Napi::Number>().Int32Value();
    bool isPressed = info[1].As<Napi::Boolean>().Value();

    WindowsKeyboardHandler::getInstance()->sendEvent(scanCode, isPressed);
}

Napi::Value IsPressed(const Napi::CallbackInfo& info) {
    int scanCode = info[0].As<Napi::Number>().Int32Value();

    bool res = WindowsKeyboardHandler::getInstance()->isPressed(scanCode);
    return Napi::Boolean::New(info.Env(), res);
}

Napi::Value IsCapsLockOn(const Napi::CallbackInfo& info) {
    bool res = WindowsKeyboardHandler::getInstance()->isCapsLockOn();
    return Napi::Boolean::New(info.Env(), res);
}

Napi::Value IsNumLockOn(const Napi::CallbackInfo& info) {
    bool res = WindowsKeyboardHandler::getInstance()->isNumLockOn();
    return Napi::Boolean::New(info.Env(), res);
}

Napi::Value IsScrollLockOn(const Napi::CallbackInfo& info) {
    bool res = WindowsKeyboardHandler::getInstance()->isScrollLockOn();
    return Napi::Boolean::New(info.Env(), res);
}

void EmitEventToJs(Napi::Env env, Napi::Function callback, std::nullptr_t* context, EventData* data) {
    if (env != NULL && callback != NULL && data != NULL) {
        callback.Call({Napi::Number::New(env, data->scanCode), Napi::Boolean::New(env, data->isPressed)});
    }

    if (data != NULL) {
        free(data);
    }
}

void EmitEventToTSCallback(int scanCode, bool isPressed) {
    EventData* data = (EventData *) malloc(sizeof(EventData));
    data->scanCode = scanCode;
    data->isPressed = isPressed;

    ts_callback.BlockingCall(data);
}

Napi::Value StartReadingEvents(const Napi::CallbackInfo& info) {
    Napi::Env env = info.Env();
    ts_callback = TSFN::New(env, info[0].As<Napi::Function>(), "StartReadingEvents", 0, 1);

    int res = WindowsKeyboardHandler::getInstance()->startReadingEvents(EmitEventToTSCallback);
    return Napi::Number::New(env, res);
}

void StopReadingEvents(const Napi::CallbackInfo& info) { WindowsKeyboardHandler::getInstance()->stopReadingEvents(); }

Napi::Object InitModule(Napi::Env env, Napi::Object exports) {
    exports["send"] = Napi::Function::New(env, Send);
    exports["isPressed"] = Napi::Function::New(env, IsPressed);
    exports["isCapsLockOn"] = Napi::Function::New(env, IsCapsLockOn);
    exports["isNumLockOn"] = Napi::Function::New(env, IsNumLockOn);
    exports["isScrollLockOn"] = Napi::Function::New(env, IsScrollLockOn);
    exports["nativeStartReadingEvents"] = Napi::Function::New(env, StartReadingEvents);
    exports["nativeStopReadingEvents"] = Napi::Function::New(env, StopReadingEvents);

    return exports;
}

#define MODULE_NAME KeyboardKtWindows ## ARCH
NODE_API_MODULE(MODULE_NAME, InitModule)

#ifdef __cplusplus
}
#endif
