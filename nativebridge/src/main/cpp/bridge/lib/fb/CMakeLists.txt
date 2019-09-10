
set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS} -DLOG_TAG=\"libfb\"")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DDISABLE_CPUCAP -DDISABLE_XPLAT -fexceptions -frtti")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-unused-parameter")

if (${TOOLCHAIN_PERMISSIVE} true)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-error=unused-but-set-variable")
endif ()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DHAVE_POSIX_CLOCKS")
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})

message(FB_CMAKE_C_FLAGS:${CMAKE_C_FLAGS})


set(LOCAL_EXPORT_LDLIBS "${LOCAL_EXPORT_LDLIBS} -llog")

set(LOCAL_SRC_FILES assert.cpp
        jni/ByteBuffer.cpp
        jni/Countable.cpp
        jni/Environment.cpp
        jni/Exceptions.cpp
        jni/fbjni.cpp
        jni/Hybrid.cpp
        jni/jni_helpers.cpp
        jni/LocalString.cpp
        jni/OnLoad.cpp
        jni/ReadableByteChannel.cpp
        jni/References.cpp
        jni/WeakReference.cpp
        log.cpp
        lyra/lyra.cpp
        onload.cpp)

include_directories("./include/")

add_library(fb SHARED ${LOCAL_SRC_FILES})

target_link_libraries(fb
        log
        dl
        android)

