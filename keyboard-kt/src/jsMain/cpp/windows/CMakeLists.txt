cmake_minimum_required(VERSION 3.10)
project(KeyboardKt)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s")

option(ARCH "architecture")

add_definitions(-D_WIN32_WINNT=0x600)
add_definitions(-DNAPI_VERSION=5)
add_definitions(-DARCH=${ARCH})

include_directories(${CMAKE_JS_INC})
include_directories($ENV{NODE_ADDON_API_HEADERS_DIR})
include_directories("../../../nativeCommon/windows")

add_library(
    ${PROJECT_NAME} SHARED
    JsKeyboardHandler.cpp
    ${CMAKE_JS_SRC}
)

set_target_properties(${PROJECT_NAME} PROPERTIES PREFIX "" SUFFIX ".node")
target_link_libraries(${PROJECT_NAME} ${CMAKE_JS_LIB})
