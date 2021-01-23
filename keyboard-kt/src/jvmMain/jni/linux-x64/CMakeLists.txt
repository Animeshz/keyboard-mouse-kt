cmake_minimum_required(VERSION 3.10)
project(KeyboardKtx64)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s")

include_directories(KeyboardKtx64 PRIVATE $ENV{JNI_HEADERS_DIR})
include_directories(KeyboardKtx64 PRIVATE "../../generated/jni")
include_directories(KeyboardKtx64 PRIVATE "../../../nativeCommon/linux")

include_directories($ENV{X11_HEADERS_DIR})
include_directories($ENV{X11_HEADERS_DIR}/extensions)

add_library(
    KeyboardKtx64 SHARED
    JvmKeyboardHandler.cpp
)
