project(xdelta3)

set(CMAKE_C_FLAGS "-O2 -std=c99 -DHAVE_LZMA_H")

include_directories("../xz/xz/src/liblzma/api/")

add_library(xdelta3 SHARED xdelta3.c)

target_link_libraries(xdelta3 lzma)