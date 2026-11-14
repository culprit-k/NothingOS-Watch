#!/usr/bin/env bash
# Assemble NothingOS Wear sources inside the public Actions SDK template.
# This selects a generic development board for compile bring-up only.
# It is NOT a CMF Watch Pro 2 D398 firmware build or flash package.
set -euo pipefail

HERE="$(cd "$(dirname "$0")/.." && pwd)"
SDK_REPO="${1:-$HERE/../../../third_party/lv_port_actions_technology}"

if [ ! -d "$SDK_REPO/action_technology_sdk" ]; then
    git clone --depth 1 https://github.com/lvgl/lv_port_actions_technology.git "$SDK_REPO"
fi

SDK_REPO="$(cd "$SDK_REPO" && pwd)"
TEMPLATE="$SDK_REPO/hello_world"
APP_DIR="$SDK_REPO/action_technology_sdk/application/app_demo/nothingos_wear"

test -d "$TEMPLATE"
rm -rf "$APP_DIR"
cp -R "$TEMPLATE" "$APP_DIR"

cp "$HERE/src/main.c" "$HERE/src/app_msg.h" "$HERE/src/app_msg.c" \
   "$HERE/src/ui_state.h" "$HERE/src/ui_state.c" "$APP_DIR/src/"

# Replace the template source list explicitly. A text substitution here can
# accidentally list app_msg.c twice because it is already in hello_world.
cat > "$APP_DIR/src/CMakeLists.txt" <<'EOF'
# SPDX-License-Identifier: Apache-2.0
zephyr_include_directories(.)
target_sources(app PRIVATE
    main.c
    app_msg.c
    ui_state.c
)
EOF

sed -i 's/project(hello_world)/project(nothingos_wear)/' "$APP_DIR/CMakeLists.txt"
cat "$HERE/prj.extra.conf" >> "$APP_DIR/prj.conf"

cat > "$SDK_REPO/action_technology_sdk/zephyr/.build_config" <<'EOF'
BOARD=ats3089c_dev_watch
APPLICATION=app_demo
SUB_APP=nothingos_wear
APP_CONF=
EOF

printf 'Assembled source at: %s\n' "$APP_DIR"
printf '%s\n' 'Compile target: ats3089c_dev_watch (generic development board only).'
printf '%s\n' 'This is not the CMF Watch Pro 2 D398 board.'
printf '%s\n' 'Do not flash any output from this assembly.'
