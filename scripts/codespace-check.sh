#!/usr/bin/env bash
set -euo pipefail

printf '\n=== NothingOS Wear: cloud workspace diagnostic ===\n'
printf 'Date (UTC): '; date -u '+%Y-%m-%d %H:%M:%S'
printf 'OS: '; . /etc/os-release && printf '%s %s\n' "$NAME" "$VERSION_ID"
printf '\n--- Tool availability ---\n'
for tool in git bash python3 cmake ninja west dtc arm-none-eabi-gcc; do
  if command -v "$tool" >/dev/null 2>&1; then
    printf '[found] %-18s %s\n' "$tool" "$(command -v "$tool")"
  else
    printf '[missing] %s\n' "$tool"
  fi
done

printf '\n--- Repository state ---\n'
git status --short --branch || true
printf '\n--- Project files ---\n'
for path in README.md docs/DEVICE_PROFILE.md docs/FIRMWARE_BASELINE.md docs/INSTALLATION_READINESS.md firmware/README.md simulator/index.html; do
  if [ -f "$path" ]; then printf '[present] %s\n' "$path"; else printf '[missing] %s\n' "$path"; fi
done

printf '\n--- Firmware readiness ---\n'
if find firmware -maxdepth 3 -type f \\( -name west.yml -o -name CMakeLists.txt -o -name prj.conf -o -name Kconfig \\) -print -quit 2>/dev/null | grep -q .; then
  echo 'A Zephyr project configuration was detected. This does not prove D398 compatibility.'
else
  echo 'No Zephyr application build definition is present yet.'
fi

echo
echo 'Diagnostic complete. No packages installed, no firmware built, and no watch connected.'
echo 'Do not flash any image until D398 board, bootloader, OTA format, signature, and recovery are verified.'
