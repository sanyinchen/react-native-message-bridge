cmake_minimum_required(VERSION 3.4.1)
project(nativebridge)



include_directories(./src/main/cpp/bridge/lib)
include_directories(./src/main/cpp/bridge/lib/fb/include)


add_subdirectory(./src/main/cpp/bridge)