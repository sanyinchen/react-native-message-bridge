cmake_minimum_required(VERSION 3.4.1)
project(nativebridge)



include_directories(src/main/cpp/first-party/fb/include)
include_directories(src/main/cpp/folly)

add_subdirectory(src/main/cpp/first-party/fb)
add_subdirectory(src/main/cpp)