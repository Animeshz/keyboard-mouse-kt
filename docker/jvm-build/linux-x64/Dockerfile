FROM dockcross/linux-x64

LABEL maintainer="Animesh Sahu animeshsahu19@yahoo.com"

ENV DEFAULT_DOCKCROSS_IMAGE animeshz/keyboard-mouse-kt:jni-build-linux-x64
ENV WORK_DIR=/work
ENV JNI_HEADERS_DIR=${WORK_DIR}/support-files/headers/jni
ENV X11_HEADERS_DIR=/usr/include/X11/

RUN \
    apt update && \
    apt install --no-install-recommends --yes \
    curl \
    libx11-dev \
    libxi-dev \
    libxtst-dev && \
    mkdir -p ${JNI_HEADERS_DIR} && \
    cd ${JNI_HEADERS_DIR} && \
    curl 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/share/native/include/jni.h' > jni.h && \
    curl 'https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/unix/native/include/jni_md.h' > jni_md.h

WORKDIR ${WORK_DIR}
