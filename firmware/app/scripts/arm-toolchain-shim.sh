#!/usr/bin/env bash
# Configure Ubuntu's distro arm-none-eabi GCC for the Actions Zephyr 2.7 SDK.
# Based on the public Linux build workaround documented by CMF-Open.
# This only configures the toolchain; it does not select a D398 target.
set -euo pipefail

SHIM="${NOTHINGOS_TOOLCHAIN_SHIM:-$HOME/.cache/nothingos-wear/gnuarmemb}"
GCC="$(command -v arm-none-eabi-gcc)" || {
    echo "arm-none-eabi-gcc is not installed" >&2
    return 1 2>/dev/null || exit 1
}
BIN_DIR="$(dirname "$GCC")"
SYSROOT="$(dirname "$(dirname "$GCC")")/lib/arm-none-eabi"
if [ ! -d "$SYSROOT/lib" ]; then
    SYSROOT="$(dirname "$(dirname "$GCC")")/arm-none-eabi"
fi
if [ ! -d "$SYSROOT/lib" ]; then
    echo "Cannot find the arm-none-eabi newlib sysroot" >&2
    return 1 2>/dev/null || exit 1
fi

mkdir -p "$SHIM"
ln -sfn "$BIN_DIR" "$SHIM/bin"
ln -sfn "$SYSROOT" "$SHIM/arm-none-eabi"

export ZEPHYR_TOOLCHAIN_VARIANT=gnuarmemb
export GNUARMEMB_TOOLCHAIN_PATH="$SHIM"
printf 'Configured GNU Arm toolchain shim at %s\n' "$SHIM"
