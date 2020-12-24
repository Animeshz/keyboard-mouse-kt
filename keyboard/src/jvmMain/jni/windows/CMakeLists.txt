cmake_minimum_required(VERSION 3.10)
project(keyboard_kt VERSION ${VERSION})

set(CMAKE_SYSTEM_NAME WindowsStore)
set(CMAKE_SYSTEM_VERSION 10.0)
set(CMAKE_SYSTEM_PROCESSOR amd64)

add_library(
    keyboard_kt SHARED
    Test.cpp
)

find_package(JNI)

if (JNI_FOUND)
    target_include_directories(keyboard_kt PRIVATE ${JNI_INCLUDE_DIRS})
endif()

target_include_directories(keyboard_kt PRIVATE "../../generated/jni")

