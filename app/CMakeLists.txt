cmake_minimum_required(VERSION 3.4.1)
add_library( xdelta3 SHARED src/main/cpp/xdelta3.c )
find_library( log-lib log )
target_link_libraries( xdelta3 ${log-lib} )