#include <napi.h>

#include "LinuxKeyboardHandler.cpp"

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

    LinuxKeyboardHandler::getInstance()->sendEvent(scanCode, isPressed);
}

Napi::Value IsPressed(const Napi::CallbackInfo& info) {
    int scanCode = info[0].As<Napi::Number>().Int32Value();

    bool res = LinuxKeyboardHandler::getInstance()->isPressed(scanCode);
    return Napi::Boolean::New(info.Env(), res);
}

Napi::Value IsCapsLockOn(const Napi::CallbackInfo& info) {
    bool res = LinuxKeyboardHandler::getInstance()->isCapsLockOn();
    return Napi::Boolean::New(info.Env(), res);
}

Napi::Value IsNumLockOn(const Napi::CallbackInfo& info) {
    bool res = LinuxKeyboardHandler::getInstance()->isNumLockOn();
    return Napi::Boolean::New(info.Env(), res);
}

Napi::Value IsScrollLockOn(const Napi::CallbackInfo& info) {
    bool res = LinuxKeyboardHandler::getInstance()->isScrollLockOn();
    return Napi::Boolean::New(info.Env(), res);
}

int Init() {
    int ret = 1;
    if (LinuxKeyboardHandler::getInstance() != NULL) ret = 0;

    return ret;
}

void EmitEventToJs(Napi::Env env, Napi::Function callback, std::nullptr_t* context, EventData* data) {
    if (env != NULL && callback != NULL && data != NULL) {
        callback.Call({Napi::Number::New(env, data->scanCode), Napi::Boolean::New(env, data->isPressed)});
    }

    if (data != NULL) {
        delete data;
    }
}

void EmitEventToTSCallback(int scanCode, bool isPressed) {
    EventData* data = new EventData;
    data->scanCode = scanCode;
    data->isPressed = isPressed;

    ts_callback.BlockingCall(data);
}

Napi::Value StartReadingEvents(const Napi::CallbackInfo& info) {
    Napi::Env env = info.Env();
    ts_callback = TSFN::New(env, info[0].As<Napi::Function>(), "StartReadingEvents", 0, 1);

    int res = LinuxKeyboardHandler::getInstance()->startReadingEvents(EmitEventToTSCallback);
    return Napi::Number::New(env, res);
}

void StopReadingEvents(const Napi::CallbackInfo& info) { LinuxKeyboardHandler::getInstance()->stopReadingEvents(); }

Napi::Object InitModule(Napi::Env env, Napi::Object exports) {
    if (env.GetInstanceData<int>() == NULL){
        env.SetInstanceData<int>(0);
        if (Init() != 0) Napi::Error::New(env, "Native module can't be initialized").ThrowAsJavaScriptException();
    }

    exports["send"] = Napi::Function::New(env, Send);
    exports["isPressed"] = Napi::Function::New(env, IsPressed);
    exports["isCapsLockOn"] = Napi::Function::New(env, IsCapsLockOn);
    exports["isNumLockOn"] = Napi::Function::New(env, IsNumLockOn);
    exports["isScrollLockOn"] = Napi::Function::New(env, IsScrollLockOn);
    exports["nativeStartReadingEvents"] = Napi::Function::New(env, StartReadingEvents);
    exports["nativeStopReadingEvents"] = Napi::Function::New(env, StopReadingEvents);

    return exports;
}

#define MODULE_NAME KeyboardKtLinux ## ARCH
NODE_API_MODULE(MODULE_NAME, InitModule)

#ifdef __cplusplus
}
#endif
