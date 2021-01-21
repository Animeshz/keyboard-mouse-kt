FROM dockcross/windows-shared-x64

LABEL maintainer="Animesh Sahu animeshsahu19@yahoo.com"

ENV DEFAULT_DOCKCROSS_IMAGE animeshz/keyboard-mouse-kt:js-build-windows-x64

RUN \
    apt-get update && \
    apt-get install --no-install-recommends --yes \
    python3 \
    nodejs \
    npm && \
    npm install -g node-gyp
