FROM dockcross/windows-shared-x86

LABEL maintainer="Animesh Sahu animeshsahu19@yahoo.com"

ENV DEFAULT_DOCKCROSS_IMAGE animeshz/keyboard-mouse-kt:cross-build-windows-x86
ENV WORK_DIR=/work
ENV JNI_HEADERS_DIR=${WORK_DIR}/support-files/headers/jni
ENV NODE_ADDON_API_HEADERS_DIR=${WORK_DIR}}/support-files/headers/node-addon-api
ENV WINDOWS_NODE_LINK_DIR=${WORK_DIR}/support-files/link/node

COPY keyboard-kt/src/jsMain/cpp/windows/node.def ${WINDOWS_NODE_LINK_DIR}/node.def

RUN \
    apt-get update && \
    curl -sL https://deb.nodesource.com/setup_lts.x | bash - && \
    apt-get install --no-install-recommends --yes \
    curl \
    python3 \
    unzip \
    nodejs && \
    npm install -g cmake-js && \
    # Download and pack JNI headers
    mkdir -p ${JNI_HEADERS_DIR} && \
    cd ${JNI_HEADERS_DIR} && \
    curl -LO 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/share/native/include/jni.h' && \
    curl -LO 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/windows/native/include/jni_md.h' && \
    # Download and pack node-addon-api headers
    mkdir -p ${NODE_ADDON_API_HEADERS_DIR} && \
    cd ${NODE_ADDON_API_HEADERS_DIR} && \
    curl -LO 'https://registry.npmjs.org/node-addon-api/-/node-addon-api-3.1.0.tgz' && \
    tar -xzvf node-addon-api-3.1.0.tgz --strip-components=1 && \
    rm node-addon-api-3.1.0.tgz && \
    # Download, build and pack linkable object file
    cd ${WINDOWS_NODE_LINK_DIR} && \
    curl -LO 'https://nodejs.org/dist/v14.15.4/node-v14.15.4-win-x86.zip' && \
    unzip node-v14.15.4-win-x86.zip "node-v14.15.4-win-x86/node.exe" && \
    i686-w64-mingw32.shared-dlltool -d node.def -y node.a && \
    rm node-v14.15.4-win-x86.zip && \
    rm -rf node-v14.15.4-win-x86 && \
    rm node.def

WORKDIR ${WORK_DIR}
