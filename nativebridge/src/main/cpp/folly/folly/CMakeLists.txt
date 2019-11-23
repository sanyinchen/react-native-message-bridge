

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fexceptions -fno-omit-frame-pointer -frtti -Wno-sign-compare")
set(FOLLY_FLAGS "${FOLLY_FLAGS} -DFOLLY_NO_CONFIG -DFOLLY_HAVE_CLOCK_GETTIME -DFOLLY_HAVE_MEMRCHR -DFOLLY_USE_LIBCPP")
set(FOLLY_FLAGS "${FOLLY_FLAGS} -DFOLLY_HAVE_XSI_STRERROR_R")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${FOLLY_FLAGS}")

message(==========>FOLLY_CMAKE_C_FLAGS:${CMAKE_C_FLAGS})


include_directories(./)
include_directories(../double-conversion/)

add_library(folly_json
        SHARED
        folly/json.cpp
        folly/Unicode.cpp
        folly/Conv.cpp
        folly/Demangle.cpp
        folly/memory/detail/MallocImpl.cpp
        folly/String.cpp
        folly/dynamic.cpp
        folly/Format.cpp
        folly/json_pointer.cpp
        folly/lang/ColdClass.cpp
        folly/detail/Demangle.cpp
        folly/hash/SpookyHashV2.cpp
        folly/container/detail/F14Table.cpp
        folly/ScopeGuard.cpp
        )

target_link_libraries(folly_json
        glog
        double-conversion
        boost)