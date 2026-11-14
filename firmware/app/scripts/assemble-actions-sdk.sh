#!/usr/bin/env bash
# Assemble NothingOS Wear sources inside the public Actions SDK template.
# This deliberately selects the generic development board only; it is NOT D398 firmware.
set -euo pipefail
HERE="$(cd "$(dirname "$0")/.." && pwd)"
SDK_REPO="${1:-$HERE/../../../third_party/lv_port_actions_technology}"
if [ ! -d "$SDK_REPO/action_technology_sdk" ]; then
  git clone --depth 1 https://github.com/lvgl/lv_port_actions_technology.git "$SDK_REPO"
fi
SDK_REPO="$(cd "$SDK_REPO" && pwd)"
APP_DIR="$SDK_REPO/action_technology_sdk/application/app_demo/nothingos_wear"
test -d "$SDK_REPO/hello_world"
rm -rf "$APP_DIR"
cp -r "$SDK_REPO/hello_world" "$APP_DIR"
cp "$HERE/src/main.c" "$HERE/src/app_msg.h" "$HERE/src/app_msg.c" "$HERE/src/ui_state.h" "$HERE/src/ui_state.c" "$APP_DIR/src/"
sed -i 's/project(hello_world)/project(nothingos_wear)/' "$APP_DIR/CMakeLists.txt"
sed -i 's/main.c/main.c app_msg.c ui_state.c/' "$APP_DIR/src/CMakeLists.txt"
cat "$HERE/prj.extra.conf" >> "$APP_DIR/prj.conf"
cat > "$SDK_REPO/action_technology_sdk/zephyr/.build_config" <<'EOF'
BOARD=ats3089c_dev_watch
APPLICATION=app_demo
SUB_APP=nothingos_wear
APP_CONF=
EOF
printf 'Assembled source at: %s\n' "$APP_DIR"
printf '%s\n' 'WARNING: ats3089c_dev_watch is a development board, not CMF Watch Pro 2 D398.'
printf '%s\n' 'This output must never be flashed to the physical watch.'
