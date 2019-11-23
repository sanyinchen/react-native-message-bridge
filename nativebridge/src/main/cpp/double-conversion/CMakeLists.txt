
set(CXX11_FLAGS "${CXX11_FLAGS} -Wno-unused-variable -Wno-unused-local-typedefs")
set(CMAKE_C_FLAGS ${CXX11_FLAGS})
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})

include_directories(./)

add_library(double-conversion
        STATIC
        double-conversion/bignum.cc
        double-conversion/bignum-dtoa.cc
        double-conversion/cached-powers.cc
        double-conversion/diy-fp.cc
        double-conversion/double-conversion.cc
        double-conversion/fast-dtoa.cc
        double-conversion/fixed-dtoa.cc
        double-conversion/strtod.cc
        )