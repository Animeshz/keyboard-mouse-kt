cmake_minimum_required(VERSION 3.10)
project(keyboard_kt VERSION ${VERSION})

set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_PROCESSOR x86_64)

set(triple x86_64-linux-gnu)

set(CMAKE_C_COMPILER clang)
set(CMAKE_C_COMPILER_TARGET ${triple})
set(CMAKE_CXX_COMPILER clang++)
set(CMAKE_CXX_COMPILER_TARGET ${triple})

add_library(
    keyboard_kt SHARED
    Test.cpp
)

find_package(JNI)

if (JNI_FOUND)
    target_include_directories(keyboard_kt PRIVATE ${JNI_INCLUDE_DIRS})
endif()

target_include_directories(keyboard_kt PRIVATE "../../generated/jni")

