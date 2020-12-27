import os
import argparse
import re
from pathlib import Path

file = Path.cwd() / 'build.txt'
if file.exists():
    print('File already generated!')
    exit(0)

parser = argparse.ArgumentParser(description='Generates cross file for cross compiling C++ (Jni)')
parser.add_argument('-s', '--system', help='name of the system, e.g. linux, windows', type=str)
args = parser.parse_args()

system = args.system

konan_dependencies = Path.home() / '.konan' / 'dependencies'
folder_name = 'mingw' if 'windows' in system else system

clang_regex = re.compile(r"(.*clang(?:\.exe)*)$", re.MULTILINE)
gcc_regex = re.compile(r'(.*gcc(?:\.exe)*)$', re.MULTILINE)


def get_directory_and_c_compiler() -> [Path, str]:
    for dep in konan_dependencies.glob('*'):
        if folder_name in dep.name:
            bin_path = (dep / 'bin').absolute()

            for binary in bin_path.glob('*'):
                if match := clang_regex.match(binary.name):
                    return bin_path, match.groups()[0]
                elif match := gcc_regex.match(binary.name):
                    return bin_path, match.groups()[0]
    else:
        print('Konan compiler dependency not found for system: ' + system)
        exit(1)


bin_dir, c_compiler = get_directory_and_c_compiler()

to_write = f'''\
[host_machine]
system = '{args.system}'
cpu_family = 'x86_64'
cpu = 'x86_64'
endian = 'little'

[binaries]
c    = '{str((bin_dir / c_compiler).absolute())}'
cpp  = '{str((bin_dir / c_compiler.replace('gcc', 'g++').replace('clang', 'clang++')).absolute())}'
'''

with file.open('w+') as f:
    f.write(to_write)

print('File generated!')
