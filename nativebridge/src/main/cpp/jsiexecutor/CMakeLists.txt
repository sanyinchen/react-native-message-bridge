


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden -fexceptions -frtti -DFOLLY_NO_CONFIG=1")
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})

include_directories(./*)

file(GLOB SOURCE ./*.cpp)
add_library(jscexecutor
        SHARED
        ${SOURCE}
        )


target_link_libraries(jscexecutor
        jsi
        jsireact
        folly_json
        fb
        reactnativejni
        )