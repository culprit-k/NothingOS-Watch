#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_DIR="${CMF_WATCH_SDK:-$ROOT/../cmf-watch-sdk}"
APP_SRC="$ROOT/fmctools/nothing-music"
APP_DST="$SDK_DIR/sdk/examples/nothing-music"
if [[ ! -f "$SDK_DIR/sdk/Cargo.toml" || ! -x "$SDK_DIR/scripts/build-app" ]]; then
  echo "cmf-watch-sdk checkout not found at: $SDK_DIR" >&2
  echo "Clone https://github.com/fmctools/cmf-watch-sdk or set CMF_WATCH_SDK." >&2
  exit 2
fi
mkdir -p "$SDK_DIR/sdk/examples"
mkdir -p "$APP_DST"
cp -R "$APP_SRC/." "$APP_DST/"
python3 - "$SDK_DIR/sdk/Cargo.toml" <<'PY'
from pathlib import Path
import sys
p = Path(sys.argv[1])
s = p.read_text()
member = '"examples/nothing-music"'
if member not in s:
    anchor = '"examples/hello",'
    if anchor not in s:
        raise SystemExit("Could not find SDK workspace member insertion point")
    p.write_text(s.replace(anchor, anchor + " " + member + ",", 1))
PY
cd "$SDK_DIR"
nix-shell -p llvm --run 'scripts/build-app nothing-music 1'
uv run --locked wp2apps info target/nothing-music.wp2app
echo "Built: $SDK_DIR/target/nothing-music.wp2app"
