cmake_minimum_required(VERSION 3.10)
project(KeyboardKt)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -s")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s")

option(ARCH "architecture")

set(TARGET ${PROJECT_NAME}Linux${ARCH})

add_definitions(-DARCH=${ARCH})

include_directories(${CMAKE_JS_INC})
include_directories($ENV{NODE_ADDON_API_HEADERS_DIR})
include_directories("../../../nativeCommon/linux")

add_library(
    ${TARGET} SHARED
    JsKeyboardHandler.cpp
    ${CMAKE_JS_SRC}
)

set_target_properties(${TARGET} PROPERTIES PREFIX "" SUFFIX ".node")
target_link_libraries(${TARGET} ${CMAKE_JS_LIB})
