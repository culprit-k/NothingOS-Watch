#!/usr/bin/env bash
# Assemble NothingOS Wear sources inside a pinned public Actions SDK template.
# The selected ATS3089C development board is compile bring-up ONLY.
# It is NOT the CMF Watch Pro 2 D398 and must never be flashed to that watch.
set -euo pipefail

HERE="$(cd "$(dirname "$0")/.." && pwd)"
SDK_REPO="${1:-$HERE/../../../third_party/lv_port_actions_technology}"
SDK_URL="https://github.com/lvgl/lv_port_actions_technology.git"
SDK_REF="26f51e584940d5b141cd1d09d2629d69b595f579"
BOARD="ats3089c_dev_watch"

if [ -e "$SDK_REPO" ] && [ ! -d "$SDK_REPO/.git" ]; then
    printf 'Refusing to use a non-git SDK directory: %s\n' "$SDK_REPO" >&2
    exit 2
fi

if [ ! -d "$SDK_REPO/.git" ]; then
    mkdir -p "$(dirname "$SDK_REPO")"
    git clone --no-checkout "$SDK_URL" "$SDK_REPO"
fi

git -C "$SDK_REPO" fetch --depth 1 origin "$SDK_REF"
git -C "$SDK_REPO" checkout --detach "$SDK_REF"

SDK_REPO="$(cd "$SDK_REPO" && pwd)"
TEMPLATE="$SDK_REPO/hello_world"
SDK_ROOT="$SDK_REPO/action_technology_sdk/zephyr"
APP_DIR="$SDK_REPO/action_technology_sdk/application/app_demo/nothingos_wear"

test -d "$TEMPLATE"
test -d "$SDK_ROOT/boards/arm/$BOARD"
test -s "$TEMPLATE/boards/$BOARD.conf"

rm -rf "$APP_DIR"
cp -R "$TEMPLATE" "$APP_DIR"

cp "$HERE/src/main.c" "$HERE/src/app_msg.h" "$HERE/src/app_msg.c" \
   "$HERE/src/ui_state.h" "$HERE/src/ui_state.c" \
   "$HERE/src/music_service.h" "$HERE/src/music_service.c" \
   "$HERE/src/timer_service.h" "$HERE/src/timer_service.c" \
   "$APP_DIR/src/"

# Replace the template source list explicitly so no file is duplicated.
cat > "$APP_DIR/src/CMakeLists.txt" <<'EOF'
# SPDX-License-Identifier: Apache-2.0
zephyr_include_directories(.)
target_sources(app PRIVATE
    main.c
    app_msg.c
    ui_state.c
    music_service.c
    timer_service.c
)
EOF

sed -i 's/project(hello_world)/project(nothingos_wear)/' "$APP_DIR/CMakeLists.txt"

# Use the project configuration rather than inheriting the template Kconfig.
# Ensure a line boundary before appending the optional additions.
cp "$HERE/prj.conf" "$APP_DIR/prj.conf"
printf '\\n' >> "$APP_DIR/prj.conf"
cat "$HERE/prj.extra.conf" >> "$APP_DIR/prj.conf"

cat > "$SDK_ROOT/.build_config" <<EOF
BOARD=$BOARD
APPLICATION=app_demo
SUB_APP=nothingos_wear
APP_CONF=
EOF

printf 'Pinned Actions SDK: %s\n' "$SDK_REF"
printf 'Assembled source at: %s\n' "$APP_DIR"
printf 'Compile target: %s (generic development board only).\n' "$BOARD"
printf '%s\n' 'The public SDK manifest references vendor dependencies on private SSH hosts.'
printf '%s\n' 'A complete build requires access to those vendor repositories and toolchains.'
printf '%s\n' 'This is not the CMF Watch Pro 2 D398 board.'
printf '%s\n' 'Do not flash any output from this assembly.'
