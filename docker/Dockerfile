FROM animeshz/mainstream-cross-compilers:SNAPSHOT

ENV JNI_HEADERS_DIR=${WORK_DIR}/support-files/headers/jni \
    NODE_ADDON_API_HEADERS_DIR=${WORK_DIR}/support-files/headers/node-addon-api

ENV X11_HEADERS_DIR=/usr/include/X11/ \
    WINDOWS_NODE_LINK_DIR=${WORK_DIR}/support-files/link/node

LABEL maintainer="Animesh Sahu animeshsahu19@yahoo.com"

COPY keyboard-mouse-kt/src/jsMain/cpp/windows/node.def ${WINDOWS_NODE_LINK_DIR}/node.def

RUN \
apt-get update && \
apt-get install --no-install-recommends --yes \
    ca-certificates \
    curl \
    libx11-dev \
    libxi-dev \
    libxtst-dev \
    python3 \
    unzip && \
curl -sL https://deb.nodesource.com/setup_lts.x | bash - && \
#
# Install Node and Cmake-JS
apt-get install --no-install-recommends -y nodejs && \
npm install -g cmake-js && \
#
# Download JNI Headers
mkdir -p ${JNI_HEADERS_DIR} && \
cd ${JNI_HEADERS_DIR} && \
mkdir -p linux && \
mkdir -p windows && \
curl 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/share/native/include/jni.h' > jni.h && \
curl 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/unix/native/include/jni_md.h' > linux/jni_md.h && \
curl 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/windows/native/include/jni_md.h' > windows/jni_md.h && \
#
# Download Node Addon API Headers
mkdir -p ${NODE_ADDON_API_HEADERS_DIR} && \
cd ${NODE_ADDON_API_HEADERS_DIR} && \
curl -LO 'https://registry.npmjs.org/node-addon-api/-/node-addon-api-3.1.0.tgz' && \
tar -xzvf node-addon-api-3.1.0.tgz --strip-components=1 && \
rm node-addon-api-3.1.0.tgz && \
#
# Download, build and pack linkable object file for node in windows
cd ${WINDOWS_NODE_LINK_DIR} && \
curl -LO 'https://nodejs.org/dist/v14.18.1/node-v14.18.1-win-x64.zip' && \
unzip node-v14.18.1-win-x64.zip "node-v14.18.1-win-x64/node.exe" && \
/opt/mxe/usr/bin/x86_64-w64-mingw32.shared-dlltool -d node.def -y node64.a && \
rm -rf node-v14.18.1-win-x64 node-v14.18.1-win-x64.zip && \
#
# Download, build and pack linkable object file
cd ${WINDOWS_NODE_LINK_DIR} && \
curl -LO 'https://nodejs.org/dist/v14.18.1/node-v14.18.1-win-x86.zip' && \
unzip node-v14.18.1-win-x86.zip "node-v14.18.1-win-x86/node.exe" && \
/opt/mxe/usr/bin/i686-w64-mingw32.shared-dlltool -d node.def -y node32.a && \
rm -rf node-v14.18.1-win-x86 node-v14.18.1-win-x86.zip && \
rm node.def && \
#
apt-get -y autoremove
