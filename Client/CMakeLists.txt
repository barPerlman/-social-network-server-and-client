cmake_minimum_required(VERSION 3.12)   # CMake version check
project(Client)               # Create project "simple_example"
set(CMAKE_CXX_STANDARD 11) # Enable c++11 standard

message(STATUS "start running cmake...")

find_package(Boost 1.65.1 COMPONENTS system filesystem REQUIRED)

if(Boost_FOUND)
    message(STATUS "Boost_INCLUDE_DIRS: ${Boost_INCLUDE_DIRS}")
    message(STATUS "Boost_LIBRARIES: ${Boost_LIBRARIES}")
    message(STATUS "Boost_VERSION: ${Boost_VERSION}")

    include_directories(${Boost_INCLUDE_DIRS})
endif()

add_executable(Client src/ConnectionHandler.cpp
        src/Client.cpp include/Client.h
        src/ClientMain.cpp)

set(SOURCE_FILES ClientMain.cpp) # Add main.cpp file of project root directory as source file

if(Boost_FOUND)
    target_link_libraries(Client ${Boost_LIBRARIES})

endif()

IF(NOT APPLE AND NOT MSVC)
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread")
ENDIF()