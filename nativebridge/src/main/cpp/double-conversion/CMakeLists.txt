
set(CXX11_FLAGS "${CXX11_FLAGS} -Wno-unused-variable -Wno-unused-local-typedefs -Werror")
set(CMAKE_C_FLAGS ${CXX11_FLAGS})
set(CMAKE_CXX_FLAGS ${CMAKE_C_FLAGS})
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu++0x -D__STDC_INT64__")

file(GLOB SOURCE "double-conversion/bignum.cc"
        "double-conversion/bignum-dtoa.cc"
        "double-conversion/cached-powers.cc"
        "double-conversion/diy-fp.cc"
        "double-conversion/double-conversion.cc"
        "double-conversion/fast-dtoa.cc"
        "double-conversion/fixed-dtoa.cc"
        "double-conversion/strtod.cc ")

add_library(double-conversion
        STATIC
        ${SOURCE}
        )