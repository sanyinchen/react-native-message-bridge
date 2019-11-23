

#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fexceptions -fno-omit-frame-pointer -frtti -Wno-sign-compare")
#set(FOLLY_FLAGS "${FOLLY_FLAGS} -DFOLLY_NO_CONFIG -DFOLLY_HAVE_CLOCK_GETTIME -DFOLLY_HAVE_MEMRCHR -DFOLLY_USE_LIBCPP")
#set(FOLLY_FLAGS "${FOLLY_FLAGS} -DFOLLY_HAVE_XSI_STRERROR_R")
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${FOLLY_FLAGS}")
#
#message(==========>FOLLY_CMAKE_C_FLAGS:${CMAKE_C_FLAGS})


include_directories(./folly)
#include_directories(../double-conversion/double-conversion)


file(GLOB SOURCE "json.cpp"
        "Unicode.cpp"
        "Conv.cpp"
        "Demangle.cpp"
        "memory/detail/MallocImpl.cpp"
        "String.cpp"
        "dynamic.cpp"
        "Format.cpp"
        "json_pointer.cpp"
        "lang/ColdClass.cpp"
        "detail/Demangle.cpp"
        "hash/SpookyHashV2.cpp"
        "container/detail/F14Table.cpp"
        "ScopeGuard.cpp"
        "lang/Assume.h")


add_library(folly_json
        SHARED
        ${SOURCE}
        )

target_link_libraries(folly_json
        glog
        double-conversion
        boost)