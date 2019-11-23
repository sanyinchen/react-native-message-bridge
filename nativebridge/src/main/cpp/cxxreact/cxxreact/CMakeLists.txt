



include_directories(./*)

file(GLOB SOURCE ./*.cpp)


add_library(reactnative
        SHARED
        ${SOURCE}
        )

#
target_link_libraries(reactnative
        glog
        folly_json
        )