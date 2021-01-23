cmake_minimum_required(VERSION 3.10)
project(KeyboardKt)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s")

option(ARCH "architecture")

include_directories($ENV{JNI_HEADERS_DIR})
include_directories("../../generated/jni")
include_directories("../../../nativeCommon/linux")

include_directories($ENV{X11_HEADERS_DIR})
include_directories($ENV{X11_HEADERS_DIR}/extensions)

add_library(
    KeyboardKt${ARCH} SHARED
    JvmKeyboardHandler.cpp
)
