cmake_minimum_required(VERSION 3.10)
project(KeyboardKtx64)

include_directories(KeyboardKtx64 PRIVATE $ENV{JNI_HEADERS_DIR})
include_directories(KeyboardKtx64 PRIVATE "../../generated/jni")

add_library(
    KeyboardKtx64 SHARED
    JvmKeyboardHandler.cpp
)
