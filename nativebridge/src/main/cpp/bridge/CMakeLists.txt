cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DANDROID")
set(FOLLY_FLAGS "${FOLLY_FLAGS} -DFOLLY_NO_CONFIG -DFOLLY_HAVE_CLOCK_GETTIME -DFOLLY_HAVE_MEMRCHR -DFOLLY_USE_LIBCPP")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${FOLLY_FLAGS}")
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})

include_directories(./core)
include_directories(./core/common)

file(GLOB SOURCE *.cpp)
add_library(corebridge
        SHARED
        ${SOURCE})
