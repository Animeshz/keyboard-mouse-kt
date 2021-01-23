FROM dockcross/windows-shared-x64

LABEL maintainer="Animesh Sahu animeshsahu19@yahoo.com"

ENV DEFAULT_DOCKCROSS_IMAGE animeshz/keyboard-mouse-kt:jni-build-windows-x64
ENV WORK_DIR=/work
ENV JNI_HEADERS_DIR=${WORK_DIR}/support-files/headers/jni
ENV NODE_ADDON_API_HEADERS_DIR=${WORK_DIR}}/support-files/headers/node-addon-api

RUN \
    apt-get update && \
    apt-get install --no-install-recommends --yes \
    curl \
    python3 \
    nodejs \
    npm && \
    npm install -g node-gyp && \
    mkdir -p ${JNI_HEADERS_DIR} && \
    cd ${JNI_HEADERS_DIR} && \
    curl 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/share/native/include/jni.h' > jni.h && \
    curl 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/windows/native/include/jni_md.h' > jni_md.h && \
    mkdir -p $NODE_ADDON_API_HEADERS_DIR && \
    npm pack node-addon-api@3.1.0 && \
    tar -xzvf node-addon-api-3.1.0.tgz --strip-components=1 && \
    rm node-addon-api-3.1.0.tgz

WORKDIR ${WORK_DIR}
