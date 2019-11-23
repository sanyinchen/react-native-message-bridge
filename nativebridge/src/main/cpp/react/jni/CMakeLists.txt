
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fexceptions -frtti -Wno-unused-lambda-capture")
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})

set(LOCAL_LDLIBS "-landroid")

include_directories(../../first-party/fb/include)
file(GLOB SOURCE *.cpp)
add_library(reactnativejni
        SHARED
        ${SOURCE})

target_link_libraries(reactnativejni fb)
