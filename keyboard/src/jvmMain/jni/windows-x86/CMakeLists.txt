cmake_minimum_required(VERSION 3.10)
project(KeyboardKtx86)

set(CMAKE_CXX_STANDARD 11)

include_directories(KeyboardKtx64 PRIVATE $ENV{JNI_HEADERS_DIR})
include_directories(KeyboardKtx64 PRIVATE "../../generated/jni")

add_library(
    KeyboardKtx86 SHARED
    JvmKeyboardHandler.cpp
)
