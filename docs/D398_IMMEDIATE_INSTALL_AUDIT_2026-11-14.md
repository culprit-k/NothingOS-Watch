# D398 immediate-installation audit — 2026-11-14

## Decision

**Do not install a custom firmware on the owner's only watch today.** No NothingOS Wear package exists in this repository. The newest public Watch Pro 2 firmware experiment is a Bad Apple demo, not NothingOS Wear, and its own release notes explicitly state that it has not been flashed to a physical watch.

## Newly verified public experiment

Repository: https://github.com/cmf-tech-community/firmware-wp2-badapple
Release: v0.1.0, published 2026-11-09
Asset: `wp2-badapple-ota.bin`
SHA-256: `563b993656f8ebfc21d4e79e9eb7eb3c8b6ce483fcfbdb10198cffecfd8c4a88`
Size: 3,851,336 bytes.

The project README describes a source-built Zephyr application for CMF Watch Pro 2 / JX402 using Actions ATS3089C. Its features are a Bad Apple video/audio demo, crown controls and BLE OTA. The release passed package checks and booted in QEMU. Its release text explicitly says the build has not been flashed to a physical watch.

The release can be inspected safely in the browser emulator:
https://cmf.arnv.dev/emulator/watch-pro-2/

This artifact is not NothingOS Wear and is not approved here for flashing to the owner's D398.

## Hardware and recovery uncertainty

The stock app/hardware profile must be established from the exact owner's official OTA for firmware 1.0.0.73. Public research requires reading the OTA's board_name, version_code, partition map, image checksums and boot-image signature status. The public firmware research itself says panel/touch/pin details and boot signature status must be established from the stock OTA, not guessed.

The watch has no external USB or recovery port according to community hardware research. A failed image may require opening the watch and using internal SWD/J-Link hardware. A phone-based OTA client is only a transport; it does not prove the image is compatible or provide physical recovery.

## Conditions before any owner-device flash

1. Obtain and hash the exact official stock 1.0.0.73 OTA from the owner's own update/cache path.
2. Confirm product board name, hardware revision, partition layout, version gates and boot signature policy from that file.
3. Build NothingOS Wear from source for the matching board profile; do not rename or relabel a generic ATS3089C dev-board image as D398-compatible.
4. Independently validate all nested container and image checksums, partition boundaries and OTA metadata.
5. Preserve a verified stock rollback package and test recovery on a donor D398 of the same hardware revision.
6. Demonstrate successful physical boot and test display, touch, crown, BLE, charging, sensors, battery and sleep/wake before using the owner's only watch.

## User setup situation

The owner has an iPhone and no computer; a temporary Android phone may become available. GitHub Codespaces is running in Chrome but the owner is not comfortable with terminal commands. Development and inspection should be automated through repository/cloud tooling where possible. Do not ask the owner to perform shell steps unless a truly unavoidable hardware-side action remains.

## Current project state

- A NothingOS Wear plan and UI simulator exist in this repository.
- The repository has cloud development-container and diagnostic scaffolding.
- No NothingOS Wear firmware binary has been built.
- No exact stock 1.0.0.73 OTA has been acquired or analyzed.
- No D398 board-specific NothingOS Wear port has been verified.
- No physical installation has been performed.
- **Owner-device flashing remains blocked.**
