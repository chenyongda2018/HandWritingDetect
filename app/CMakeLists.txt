# Sets the minimum version of CMake required to build the native library.
set(CURRENT_DIR ${CMAKE_SOURCE_DIR})
message("CURRENT_DIR:" ${CMAKE_SOURCE_DIR})
set(pathToOpenCv D:/SoftWare/Programing/OpenCV-3.0.0-android-sdk-1/OpenCV-android-sdk)#设置OpenCv的路径变量
cmake_minimum_required(VERSION 3.4.1)
#支持-std=gnu++11
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
#配置加载native依赖
include_directories(${pathToOpenCv}/sdk/native/jni/include)
#动态方式加载
add_library(lib_opencv STATIC IMPORTED ) #表示创建一个导入库，静态方式

set(lib_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs)

#引入libopencv_java3.so文件

set_target_properties(lib_opencv
                       PROPERTIES
                       IMPORTED_LOCATION ${lib_DIR}/${ANDROID_ABI}/libopencv_java3.so
                       )

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#自己的源文件
add_library( # Sets the name of the library.
             hello

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/Hello.cpp )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries( # Specifies the target library.
# 这里是你so的名字。刚才在 MainActivity里面要引用的 一样。
                       hello

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} android -ljnigraphics lib_opencv)