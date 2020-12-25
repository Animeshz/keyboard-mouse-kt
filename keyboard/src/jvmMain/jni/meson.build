project('KeyboardKt', 'cpp', license: 'MIT')

version = get_option('version')
platform = get_option('platform')
include_dirs = get_option('include_dirs')

sources = [platform / 'Test.cpp']
incl = include_directories(include_dirs)

shared_library(
    'KeyboardKt',
    sources,
    include_directories: incl
)
