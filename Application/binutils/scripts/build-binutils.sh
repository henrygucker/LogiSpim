#!/usr/bin/env bash
# Build host-native GNU binutils that manipulate mipsel-none-elf objects.

set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
BINUTILS_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd -P)"

SOURCE_DIR="${BINUTILS_SOURCE_DIR:-${BINUTILS_DIR}/target/src/binutils-$(tr -d '[:space:]' < "${BINUTILS_DIR}/.gnu_version")}"
BUILD_DIR="${BINUTILS_BUILD_DIR:-${BINUTILS_DIR}/target/build}"
OUTPUT_DIR="${BINUTILS_OUTPUT_DIR:-${BINUTILS_DIR}/target/out}"
TARGET="${BINUTILS_TARGET:-mipsel-none-elf}"
JOBS="${JOBS:-2}"
MAKE_COMMAND="${MAKE:-make}"

if [[ ! -f "${SOURCE_DIR}/configure" ]]; then
    echo "[binutils] ERROR: configure script not found at ${SOURCE_DIR}/configure" >&2
    exit 1
fi

if ! command -v "${MAKE_COMMAND}" >/dev/null 2>&1; then
    echo "[binutils] ERROR: make command not found: ${MAKE_COMMAND}" >&2
    exit 1
fi

if ! command -v makeinfo >/dev/null 2>&1; then
    echo "[binutils] ERROR: GNU Texinfo (makeinfo) is required to build GNU Binutils." >&2
    if [[ "${MSYSTEM:-}" == "UCRT64" ]]; then
        echo "[binutils] Install it with: pacman -S --needed mingw-w64-ucrt-x86_64-texinfo" >&2
    fi
    exit 1
fi

if ! makeinfo --version 2>&1 | grep -qi 'GNU texinfo'; then
    echo "[binutils] ERROR: makeinfo is present but does not report GNU Texinfo; install a supported Texinfo release." >&2
    exit 1
fi

# Use the verified executable explicitly. In particular, do not allow a
# stale MAKEINFO=true inherited from the caller to bypass configure's version
# check and be replaced with the failing "missing makeinfo" wrapper.
MAKEINFO="$(command -v makeinfo)"
export MAKEINFO

mkdir -p "${BUILD_DIR}" "${OUTPUT_DIR}"
cd "${BUILD_DIR}"

CONFIGURE_ARGS=(
    "--target=${TARGET}"
    "--prefix=${OUTPUT_DIR}"
    "--disable-nls"
    "--disable-werror"
    "--disable-gdb"
    "--disable-sim"
    "--disable-gprofng"
    "--without-zstd"
    "--enable-deterministic-archives"
)

if [[ "${MSYSTEM:-}" == "UCRT64" ]]; then
    # Match MSYS2's native MinGW package-build convention. The compiler and
    # resulting tools target Windows directly; they do not link to msys-2.0.dll.
    BUILD_TRIPLET="${MINGW_CHOST:-x86_64-w64-mingw32}"
    CONFIGURE_ARGS+=("--build=${BUILD_TRIPLET}")
else
    BUILD_TRIPLET="$(bash "${SOURCE_DIR}/config.guess")"
    CONFIGURE_ARGS+=("--build=${BUILD_TRIPLET}")
fi

echo "[binutils] Configuring GNU binutils for ${TARGET} (build host: ${BUILD_TRIPLET})"
bash "${SOURCE_DIR}/configure" "${CONFIGURE_ARGS[@]}"

echo "[binutils] Compiling with ${JOBS} job(s)"
"${MAKE_COMMAND}" -j"${JOBS}"

echo "[binutils] Installing into ${OUTPUT_DIR}"
"${MAKE_COMMAND}" install

# MinGW/UCRT64 executables are native Windows PE files. Copy the GCC runtime
# DLLs that may be imported by the tools, so the bundled tools do not require
# the end user's MSYS2 installation or its PATH.
if [[ "${MSYSTEM:-}" == "UCRT64" ]]; then
    RUNTIME_BIN="${MINGW_PREFIX:-/ucrt64}/bin"
    mkdir -p "${OUTPUT_DIR}/bin"
    for dll in \
        libgcc_s_seh-1.dll \
        libstdc++-6.dll \
        libwinpthread-1.dll; do
        if [[ -f "${RUNTIME_BIN}/${dll}" ]]; then
            cp -f "${RUNTIME_BIN}/${dll}" "${OUTPUT_DIR}/bin/${dll}"
        fi
    done
fi

# Verify that the expected target-prefixed assembler and linker were installed.
if [[ "${MSYSTEM:-}" == "UCRT64" ]]; then
    EXE_SUFFIX=".exe"
else
    EXE_SUFFIX=""
fi

for tool in as ld objcopy objdump; do
    executable="${OUTPUT_DIR}/bin/${TARGET}-${tool}${EXE_SUFFIX}"
    if [[ ! -f "${executable}" ]]; then
        echo "[binutils] ERROR: expected executable was not installed: ${executable}" >&2
        exit 1
    fi
    if [[ "${MSYSTEM:-}" != "UCRT64" ]]; then
        chmod u+x "${executable}"
    fi
done

echo "[binutils] GNU binutils build and installation completed."
