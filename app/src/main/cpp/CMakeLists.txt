cmake_minimum_required(VERSION 3.22.1)
project(ubuterm-native)

add_library(ubuterm-native SHARED
    pty_bridge.cpp
    proot_launcher.cpp
    signal_helpers.cpp
)

find_library(log-lib log)

target_link_libraries(ubuterm-native
    ${log-lib}
    android
)
