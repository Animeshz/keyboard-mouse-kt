cmake_minimum_required(VERSION 3.10)
project(KeyboardKtx64)

include_directories(KeyboardKtx64 PRIVATE $ENV{JNI_HEADERS_DIR})
include_directories(KeyboardKtx64 PRIVATE "../../generated/jni")

include_directories($ENV{X11_HEADERS_DIR})
include_directories($ENV{X11_HEADERS_DIR}/extensions)

add_library(
    KeyboardKtx64 SHARED
    JvmKeyboardHandler.cpp
)
