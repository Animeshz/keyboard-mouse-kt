cmake_minimum_required(VERSION 3.10)
project(keyboard_kt)

add_library(
    keyboard_kt SHARED
    Test.cpp
)

find_package(JNI)

if (JNI_FOUND)
    target_include_directories(keyboard_kt PRIVATE ${JNI_INCLUDE_DIRS})
endif()

target_include_directories(keyboard_kt PRIVATE "../../generated/jni")
