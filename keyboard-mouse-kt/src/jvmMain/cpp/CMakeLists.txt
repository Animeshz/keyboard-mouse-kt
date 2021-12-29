cmake_minimum_required(VERSION 3.10)
project(KeyboardMouseKt)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s -Os -fdata-sections -ffunction-sections -Wl,--gc-sections")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s -Os -fdata-sections -ffunction-sections -Wl,--gc-sections")

option(ARCH "architecture")
option(BUILD_WINDOWS "Build for windows?" FALSE)
option(BUILD_LINUX "Build for linux?" FALSE)

if(!BUILD_WINDOWS && !BUILD_LINUX)
  message(FATAL_ERROR "Must provide a OS to build. Either set BUILD_WINDOWS or BUILD_LINUX.")
endif()

if(BUILD_WINDOWS)
    add_definitions(-D_WIN32_WINNT=0x600)
    include_directories("../../../nativeCommon/windows")
elseif(BUILD_LINUX)
    include_directories($ENV{X11_HEADERS_DIR})
    include_directories($ENV{X11_HEADERS_DIR}/extensions)
    include_directories("../../../nativeCommon/linux")
endif()

include_directories($ENV{JNI_HEADERS_DIR})
include_directories($ENV{JNI_HEADERS_DIR}/windows)
include_directories("../../generated/jni")

add_library(
    ${PROJECT_NAME}${ARCH} SHARED
    KeyboardInternal.cpp
)
