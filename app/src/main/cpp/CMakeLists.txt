cmake_minimum_required(VERSION 3.22)
project(LocalMindTokenizer)

include(FetchContent)
FetchContent_Declare(
    tokenizers-cpp
    GIT_REPOSITORY https://github.com/mlc-ai/tokenizers-cpp
    GIT_TAG        main
    GIT_SUBMODULES_RECURSE TRUE
)
FetchContent_MakeAvailable(tokenizers-cpp)

add_library(tokenizer_jni SHARED tokenizer_jni.cpp)

target_include_directories(tokenizer_jni PRIVATE ${tokenizers-cpp_SOURCE_DIR}/include)

target_link_libraries(tokenizer_jni PRIVATE tokenizers_cpp log)
