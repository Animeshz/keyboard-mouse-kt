cmake_minimum_required(VERSION 3.10)
project(KeyboardKt)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s -Os -fdata-sections -ffunction-sections -Wl,--gc-sections")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s -Os -fdata-sections -ffunction-sections -Wl,--gc-sections")

option(ARCH "architecture")

add_definitions(-D_WIN32_WINNT=0x600)

include_directories($ENV{JNI_HEADERS_DIR})
include_directories($ENV{JNI_HEADERS_DIR}/windows)
include_directories("../../generated/jni")
include_directories("../../../nativeCommon/windows")

add_library(
    ${PROJECT_NAME}${ARCH} SHARED
    JvmKeyboardHandler.cpp
)
