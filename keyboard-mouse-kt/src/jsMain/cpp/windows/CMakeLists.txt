cmake_minimum_required(VERSION 3.10)
project(KeyboardMouseKt)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s -Os -fdata-sections -ffunction-sections -Wl,--gc-sections")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -static-libstdc++ -s -Os -fno-rtti -fno-exceptions -fdata-sections -ffunction-sections -Wl,--gc-sections")

option(ARCH "architecture")
option(NODE_DEF_LINK_SUFFIX "suffix of node definition file")

set(TARGET ${PROJECT_NAME}Windows${ARCH})

add_definitions(-D_WIN32_WINNT=0x600)
add_definitions(-DNAPI_VERSION=5)
add_definitions(-DARCH=${ARCH})
add_definitions(-DNAPI_DISABLE_CPP_EXCEPTIONS)

include_directories(${CMAKE_JS_INC})
include_directories($ENV{NODE_ADDON_API_HEADERS_DIR})
include_directories("../../../nativeCommon/windows")

add_library(
    ${TARGET} SHARED
    JsKeyboardHandler.cpp
    ${CMAKE_JS_SRC}
)

target_link_libraries(${TARGET} $ENV{WINDOWS_NODE_LINK_DIR}/node${NODE_DEF_LINK_SUFFIX}.a)

set_target_properties(${TARGET} PROPERTIES PREFIX "" SUFFIX ".node")
target_link_libraries(${TARGET} ${CMAKE_JS_LIB})
