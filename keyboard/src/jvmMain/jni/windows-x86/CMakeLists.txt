cmake_minimum_required(VERSION 3.10)
project(KeyboardKtx86)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s")

include_directories(KeyboardKtx86 PRIVATE $ENV{JNI_HEADERS_DIR})
include_directories(KeyboardKtx86 PRIVATE "../../generated/jni")

add_library(
    KeyboardKtx86 SHARED
    JvmKeyboardHandler.cpp
)
