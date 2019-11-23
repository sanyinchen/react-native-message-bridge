


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fexceptions -frtti -O3")
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})

include_directories(./*)

file(GLOB SOURCE jsireact/*.cpp)
add_library(jsireact
        SHARED
        ${SOURCE}
        )


target_link_libraries(jsireact
        jsi
        reactnative
        glog
        )