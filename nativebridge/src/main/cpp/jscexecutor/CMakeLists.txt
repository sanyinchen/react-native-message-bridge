


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden -fexceptions -frtti")
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})


file(GLOB SOURCE ./*.cpp)
add_library(jscexecutor
        SHARED
        ${SOURCE}
        )


#target_link_libraries(jsi
#        libjsireact
#        libfolly_json
#        libfb
#        libreactnativejni
#        )