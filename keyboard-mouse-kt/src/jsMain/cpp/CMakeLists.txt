cmake_minimum_required(VERSION 3.10)
project(KeyboardMouseKt)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s -Os -fdata-sections -ffunction-sections -Wl,--gc-sections")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s -Os -fno-rtti -fno-exceptions -fdata-sections -ffunction-sections -Wl,--gc-sections")

option(ARCH "architecture")
option(BUILD_WINDOWS "Build for windows?" FALSE)
option(BUILD_LINUX "Build for linux?" FALSE)

if(!BUILD_WINDOWS && !BUILD_LINUX)
  message(FATAL_ERROR "Must provide a OS to build. Either set BUILD_WINDOWS or BUILD_LINUX.")
endif()

if(BUILD_WINDOWS)
    option(NODE_DEF_LINK_SUFFIX "suffix of node definition file")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -static-libstdc++")
    set(TARGET ${PROJECT_NAME}Windows${ARCH})
    add_definitions(-D_WIN32_WINNT=0x600)
    include_directories("../../../nativeCommon/windows")
elseif(BUILD_LINUX)
    set(TARGET ${PROJECT_NAME}Linux${ARCH})
    include_directories(${CMAKE_JS_INC})
    include_directories($ENV{NODE_ADDON_API_HEADERS_DIR})
    include_directories("../../../nativeCommon/linux")
endif()

add_definitions(-DTARGET=${TARGET})
add_definitions(-DNAPI_VERSION=5)
add_definitions(-DNAPI_DISABLE_CPP_EXCEPTIONS)

include_directories(${CMAKE_JS_INC})
include_directories($ENV{NODE_ADDON_API_HEADERS_DIR})

add_library(
    ${TARGET} SHARED
    KeyboardInternal.cpp
    ${CMAKE_JS_SRC}
)

if(BUILD_WINDOWS)
    target_link_libraries(${TARGET} $ENV{WINDOWS_NODE_LINK_DIR}/node${NODE_DEF_LINK_SUFFIX}.a)
endif()

set_target_properties(${TARGET} PROPERTIES PREFIX "" SUFFIX ".node")
target_link_libraries(${TARGET} ${CMAKE_JS_LIB})
