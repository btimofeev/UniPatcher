# UniPatcher — Android build environment.
#
# Build the app without polluting the host machine:
#
#   docker build -t unipatcher-builder docker/
#   docker run --rm \
#       -v "$PWD":/workspace \
#       -v "$PWD/docker/local.properties.docker:/workspace/local.properties:ro" \
#       -v "$HOME/.gradle-docker":/home/build \
#       unipatcher-builder \
#       ./gradlew --no-daemon :app:downloadDependencies :app:assembleFreeDebug
#
# Or use the wrapper script in this directory (builds the image on first run
# and sets up all mounts for you, including the Gradle cache):
#
#   ./docker/build.sh                            # default gradle build
#   ./docker/build.sh ./gradlew <task>           # arbitrary gradle command
#   ./docker/build.sh opencode                   # run the opencode agent
#   ./docker/build.sh bash                       # interactive shell
#   ./docker/build.sh --rebuild                  # force a rebuild of the image
#
# The script runs the container with --user "$(id -u):$(id -g)", so build
# outputs under /workspace stay owned by the host user. The ~/.gradle-docker
# mount becomes the container's HOME: GRADLE_USER_HOME (=~/.gradle) and
# ~/.android (debug keystore is generated once and then reused) live there and
# persist between runs.
#
# local.properties is gitignored (absent from a fresh clone), so AGP normally
# picks up ANDROID_HOME inside the container. When mounting the working tree,
# the host's local.properties would point sdk.dir at the host SDK and break the
# build — the second mount overlays it with local.properties.docker, which sets
# sdk.dir to the container's /opt/android-sdk instead.

# Fall back to `eclipse-temurin:25-jdk` if the -noble tag is not available on
# your registry.
FROM eclipse-temurin:25-jdk-noble

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=$ANDROID_HOME
ENV DEBIAN_FRONTEND=noninteractive

# The container runs as the host user (see --user in docker/build.sh), so give
# it a writeable HOME. On the host this whole directory is bound from
# ~/.gradle-docker (Gradle cache + ~/.android), but keep it 0777 in the image
# so the container also works without that mount.
ENV HOME=/home/build
ENV GRADLE_USER_HOME=$HOME/.gradle
RUN mkdir -p "$HOME" && chmod 0777 "$HOME"

# System dependencies. The native code (xdelta3, xz) is pure C and is
# cross-compiled by the NDK toolchain, so no host C compiler is needed.
RUN apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        git \
        unzip \
        zip \
    && rm -rf /var/lib/apt/lists/*

# Android SDK command-line tools
ARG CMDLINE_TOOLS_VERSION=11076708
RUN mkdir -p "$ANDROID_HOME/cmdline-tools" \
    && curl -fsSL -o /tmp/cmdline-tools.zip \
        "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" \
    && unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools \
    && mv /tmp/cmdline-tools/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest" \
    && rm -rf /tmp/cmdline-tools /tmp/cmdline-tools.zip

ENV PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

# Component versions mirror app/build.gradle.kts (compileSdk 36, ndkVersion
# 29.0.14206865, cmake.version 3.31.6).
RUN yes | sdkmanager --licenses >/dev/null 2>&1 || true \
    && sdkmanager \
        "platform-tools" \
        "platforms;android-36" \
        "build-tools;36.0.0" \
        "ndk;29.0.14206865" \
        "cmake;3.31.6"

# The container runs as the host user (--user in docker/build.sh), while the
# SDK is installed by root during the build. AGP may still want to add SDK
# components at build time, so keep the tree writable by any user.
RUN chmod -R a+w "$ANDROID_HOME"

# opencode CLI (standalone binary; no Node.js needed). The wrapper script runs
# the container as the host user, so opencode must live in a world-readable
# location instead of /root.
RUN curl -fsSL https://opencode.ai/install | bash \
    && mkdir -p /opt/opencode \
    && cp -a "$HOME/.opencode/." /opt/opencode/ \
    && ln -sf /opt/opencode/bin/opencode /usr/local/bin/opencode

# The project root on the host is mounted here via `-v`.
WORKDIR /workspace