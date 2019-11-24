


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fexceptions -frtti -O3")
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})

include_directories(./*.h)


file(GLOB SOURCE *.cpp)
add_library(jsi
        SHARED
        "JSCRuntime.cpp"
        "JSIDynamic.cpp"
        "jsi.cpp"
        )


target_link_libraries(jsi
        folly_json
        jsc
        glog
        log
        )

link_libraries("libjsc.so")