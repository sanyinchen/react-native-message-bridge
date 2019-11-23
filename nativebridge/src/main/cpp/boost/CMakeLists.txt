
include_directories(./boost_1_63_0)
file(GLOB SOURCE ./boost_1_63_0/boost/*.hpp)
add_library(boost
        STATIC
        ${SOURCE})
SET_TARGET_PROPERTIES(boost PROPERTIES LINKER_LANGUAGE CXX)