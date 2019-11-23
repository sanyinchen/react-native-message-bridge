

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-unused-parameter -fexceptions -fno-omit-frame-pointer")
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})

include_directories(./)

file(GLOB SOURCE *.cpp)

add_library(glog_init
        SHARED
        ${SOURCE})

target_link_libraries(glog_init
        log
        glog)
