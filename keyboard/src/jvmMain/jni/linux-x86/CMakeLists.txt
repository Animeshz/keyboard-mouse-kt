cmake_minimum_required(VERSION 3.10)
project(KeyboardKtx86)

set(CMAKE_CXX_STANDARD 11)

include_directories(KeyboardKtx86 PRIVATE $ENV{JNI_HEADERS_DIR})
include_directories(KeyboardKtx86 PRIVATE "../../generated/jni")

include_directories($ENV{X11_HEADERS_DIR})
include_directories($ENV{X11_HEADERS_DIR}/extensions)

add_library(
    KeyboardKtx86 SHARED
    JvmKeyboardHandler.cpp
)
