cmake_minimum_required(VERSION 3.10)
project(KeyboardKtx64)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s")

add_definitions(-D_WIN32_WINNT=0x600)

include_directories(KeyboardKtx64 PRIVATE $ENV{JNI_HEADERS_DIR})
include_directories(KeyboardKtx64 PRIVATE "../../generated/jni")
include_directories(KeyboardKtx64 PRIVATE "../../../nativeCommon/windows")

add_library(
    KeyboardKtx64 SHARED
    JvmKeyboardHandler.cpp
)
