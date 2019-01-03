
cmake_minimum_required(VERSION 3.13)   # CMake version check
project(Client)               # Create project "simple_example"
set(CMAKE_CXX_STANDARD 11)            # Enable c++11 standard

set(SOURCE_FILES src/ClientMain.cpp) # Add main.cpp file of project root directory as source file

add_executable(Client src/ConnectionHandler.cpp
        include/ConnectionHandler.h
        src/Client.cpp include/Client.h
        src/ClientMain.cpp
        makefile)

set(Boost_USE_STATIC_LIBS OFF)
set(Boost_USE_MULTITHREADED ON)
set(Boost_USE_STATIC_RUNTIME OFF)
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -pthread")
find_package(Boost 1.45.0 COMPONENTS filesystem regex)

if(Boost_FOUND)
    include_directories(${Boost_INCLUDE_DIRS})
    target_link_libraries(Client ${Boost_LIBRARIES})
endif()
