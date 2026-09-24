#!/usr/bin/env bash
# Build UniPatcher in Docker without polluting the host.
# See docker/Dockerfile for details on the mounts.

set -euo pipefail

usage() {
    cat <<'EOF'
Usage: ./docker/build.sh [option] [command...]

Builds UniPatcher in a Docker container: the project tree and a persistent
Gradle cache are mounted into the container, and it runs as the current user
(project files are not created as root).

Options:
  -h, --help      show this help message
  --rebuild       force a rebuild of the "unipatcher-builder" image before running
  --skip-deps     default build without :app:downloadDependencies
  --force-deps    default build always re-running :app:downloadDependencies

Commands (everything after the options is executed inside the container,
working directory = the project root):

  (no command)    default build. Native sources (xdelta3/xz) are downloaded
                  only when they are missing from app/src/main/cpp/; then the
                  free debug APK is built:
                    ./gradlew --no-daemon [":app:downloadDependencies"] :app:assembleFreeDebug
  ./gradlew <task>
                  run an arbitrary Gradle task, e.g.: ./docker/build.sh ./gradlew test
  bash            drop into an interactive shell inside the container
  /bin/bash -c '<cmd>'
                  run a one-off shell command, e.g.: /bin/bash -c 'ls'
  opencode        launch the opencode AI coding agent (TUI) inside the container

Mounts:
  <project root>                 -> /workspace                 the project
  docker/local.properties.docker -> /workspace/local.properties  read-only (sdk.dir)
  $HOME/.gradle-docker           -> /home/build                container HOME:
                                                                  Gradle cache,
                                                                  ~/.android debug key
  host opencode config           -> /home/build/.config/opencode
                                  (only if ~/.config/opencode exists;
                                  lets opencode in the container reuse your
                                  API keys / settings)

Environment:
  GRADLE_CACHE_DIR    override the host directory used for the cache above,
                      e.g.: GRADLE_CACHE_DIR=~/docker-cache ./docker/build.sh
EOF
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(dirname "$SCRIPT_DIR")"
IMAGE_TAG="unipatcher-builder"

REBUILD=0
SKIP_DEPS=0
FORCE_DEPS=0
CMD=()
after_dashdash=0
for arg in "$@"; do
    if (( after_dashdash )); then
        CMD+=("$arg")
        continue
    fi
    case "$arg" in
        --)
            after_dashdash=1
            ;;
        -h | --help)
            usage
            exit 0
            ;;
        --rebuild)
            REBUILD=1
            ;;
        --skip-deps)
            SKIP_DEPS=1
            ;;
        --force-deps)
            FORCE_DEPS=1
            ;;
        -*)
            echo "Unknown option: $arg" >&2
            usage
            exit 1
            ;;
        *)
            CMD+=("$arg")
            ;;
    esac
done

# Native sources live inside the project (mounted as /workspace), so we can
# check on the host whether :app:downloadDependencies is still needed.
native_deps_present() {
    local c="$REPO_ROOT/app/src/main/cpp"
    for d in xdelta3/xdelta xz/xz; do
        [[ -d "$c/$d" ]] || return 1
    done
    return 0
}

if (( ${#CMD[@]} == 0 )); then
    if (( FORCE_DEPS )); then
        CMD=(./gradlew --no-daemon ":app:downloadDependencies" :app:assembleFreeDebug)
    elif (( SKIP_DEPS )); then
        CMD=(./gradlew --no-daemon :app:assembleFreeDebug)
    elif native_deps_present; then
        echo "Native dependencies already present, skipping :app:downloadDependencies" >&2
        CMD=(./gradlew --no-daemon :app:assembleFreeDebug)
    else
        CMD=(./gradlew --no-daemon ":app:downloadDependencies" :app:assembleFreeDebug)
    fi
fi

# Dedicated host cache directory, bound into the container as its HOME. It holds
# both the Gradle cache (~/.gradle) and ~/.android (the debug keystore is
# generated once and then reused, so every APK is signed with the same key).
# Kept out of the real ~/.gradle. Override with GRADLE_CACHE_DIR if you want it
# elsewhere.
GRADLE_CACHE="${GRADLE_CACHE_DIR:-$HOME/.gradle-docker}"
mkdir -p "$GRADLE_CACHE"

# Run the container as the host user so files created in /workspace and
# $GRADLE_CACHE are owned by the current user, not root.
UID_GID="$(id -u):$(id -g)"

MOUNTS=(
    -v "$REPO_ROOT":/workspace
    -v "$SCRIPT_DIR/local.properties.docker:/workspace/local.properties:ro"
    -v "$GRADLE_CACHE":/home/build
)

# Reuse the host's opencode configuration (API keys, settings) in the
# container, so `./docker/build.sh opencode` works without extra setup.
HOST_OPENCODE_CONFIG="${XDG_CONFIG_HOME:-$HOME/.config}/opencode"
if [[ -d "$HOST_OPENCODE_CONFIG" ]]; then
    MOUNTS+=(-v "$HOST_OPENCODE_CONFIG":/home/build/.config/opencode)
fi

if (( REBUILD == 1 )); then
    docker build -t "$IMAGE_TAG" "$SCRIPT_DIR"
fi

if ! docker image inspect "$IMAGE_TAG" >/dev/null 2>&1; then
    echo "Image $IMAGE_TAG not found, building it first..." >&2
    docker build -t "$IMAGE_TAG" "$SCRIPT_DIR"
fi

TTY_ARGS=()
if [[ -t 0 ]]; then
    TTY_ARGS=(-it -e TERM="${TERM:-xterm-256color}")
fi

exec docker run --rm "${TTY_ARGS[@]}" \
    --user "$UID_GID" \
    "${MOUNTS[@]}" \
    -w /workspace \
    "$IMAGE_TAG" \
    "${CMD[@]}"