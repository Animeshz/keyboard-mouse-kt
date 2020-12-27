project('KeyboardKt', 'cpp', license: 'MIT')

platform = get_option('platform')
include_dirs = get_option('include_dirs')

sources = [platform / 'Test.cpp']
incl = include_directories(include_dirs)

# if meson.get_compiler('cpp').get_id() == 'clang'
#   add_global_arguments('--target=x86_64-' + platform + '-gnu', language: 'cpp')
# endif

shared_library(
    'KeyboardKt',
    sources,
    include_directories: incl
)
