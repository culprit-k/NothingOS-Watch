#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK="${FMC_APPS_WORK:-$ROOT/target/fmctools-apps}"
APP_SRC="$ROOT/fmctools/nothing-music"

if [[ ! -f "$APP_SRC/store.toml" || ! -f "$APP_SRC/src/main.rs" ]]; then
  echo "Nothing Music app source is incomplete: $APP_SRC" >&2
  exit 2
fi

if [[ ! -d "$WORK/.git" ]]; then
  mkdir -p "$(dirname "$WORK")"
  git clone --quiet --depth 1 https://github.com/fmctools/apps "$WORK"
fi

mkdir -p "$WORK/apps/nothing-music"
cp -R "$APP_SRC/." "$WORK/apps/nothing-music/"

cd "$WORK"
make check
make test APP=nothing-music
make build APP=nothing-music

test -s target/nothing-music.wp2app
uv run --quiet --locked --project .sdk/wp2-apps wp2apps info target/nothing-music.wp2app
echo "Built package: $WORK/target/nothing-music.wp2app"
