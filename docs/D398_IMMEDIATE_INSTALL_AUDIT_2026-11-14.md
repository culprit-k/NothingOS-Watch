# D398 immediate-installation audit — 2026-11-14

## Decision

**Do not install a custom firmware on the owner's only watch.** No NothingOS Wear package exists in this repository. The public Watch Pro 2 firmware experiment is a Bad Apple demo, not NothingOS Wear, and its release notes state that it has not been flashed to a physical watch.

## Newly verified public experiment

Repository: https://github.com/cmf-tech-community/firmware-wp2-badapple  
Release: v0.1.0, published 2026-11-09  
Asset: wp2-badapple-ota.bin  
SHA-256: 563b993656f8ebfc21d4e79e9eb7eb3c8b6ce483fcfbdb10198cffecfd8c4a88  
Size: 3,851,336 bytes.

The project README describes a source-built Zephyr application for CMF Watch Pro 2 / JX402 using Actions ATS3089C. Its features are a Bad Apple video/audio demo, crown controls and BLE OTA. The release passed package checks and booted in QEMU. Its release text explicitly says the build has not been flashed to a physical watch.

The release can be inspected safely in the browser emulator:  
https://cmf.arnv.dev/emulator/watch-pro-2/

This artifact is not NothingOS Wear and is not approved here for flashing to the owner's D398.

## Current NothingOS Wear source progress

The repository now contains:
- an original Actions/Zephyr + LVGL application shell with Home, Apps, System and Timer pages;
- a five-minute countdown timer with Start/Pause/Resume/Reset behavior;
- portable timer, UI-state and two-mode music-service modules;
- strict host-side unit tests for those portable modules;
- a browser UI simulator and updated public-SDK research documentation.

The GitHub Actions source-check workflow passes the portable C tests and shell syntax validation. It does **not** compile the embedded LVGL application against the complete vendor SDK, create a firmware image, or validate anything on physical D398 hardware.

## Hardware and recovery uncertainty

The stock app/hardware profile must be established from the exact owner's official OTA for firmware 1.0.0.73. Public research requires reading the OTA's board_name, version_code, partition map, image checksums and boot-image signature status. The public firmware research itself says panel/touch/pin details and boot signature status must be established from the stock OTA, not guessed.

The public Actions SDK's west manifest points important vendor dependencies to internal SSH repositories. A plain public GitHub clone does not supply all dependencies for a full vendor build. The public generic ATS3089C development board configuration is also not proof of D398 pinout or peripheral configuration.

Community hardware research warns that a failed image may require opening the watch and using internal SWD/J-Link hardware. A phone-based OTA client is only a transport; it does not prove the image is compatible or provide physical recovery.

## Conditions before any owner-device flash

1. Confirm exact D398 hardware revision and boot-chain requirements from reliable device-specific evidence.
2. Obtain or otherwise lawfully access the exact official stock 1.0.0.73 OTA, then hash and inspect it without altering the original.
3. Confirm board_name, hardware revision, partition layout, version gates and signature policy from that package.
4. Build NothingOS Wear with the complete authorized SDK and a D398-specific board profile; do not relabel a generic ATS3089C dev-board image as D398-compatible.
5. Independently validate all nested container and image checksums, partition boundaries and OTA metadata.
6. Preserve a verified stock rollback package and test recovery on a donor D398 of the same hardware revision.
7. Demonstrate successful physical boot and test display, touch, crown, BLE, charging, sensors, battery and sleep/wake before using the owner's only watch.

## User setup situation

The owner has an iPhone and no computer; a temporary Android phone may become available. GitHub Codespaces is available in Chrome, but terminal workflows are difficult for the owner. Development and inspection should be automated through repository/cloud tooling where possible. Do not ask the owner to perform shell steps unless a truly unavoidable hardware-side action remains.

## Current project state

- Embedded application source: present, not full-SDK compile-validated.
- Portable UI, Music and Timer services: host-compiled and unit-tested in GitHub Actions.
- Public SDK source assembly: pinned and syntax-checked; full build blocked by private vendor dependencies/toolchain closure.
- Exact stock 1.0.0.73 OTA: not acquired or analyzed.
- D398-specific board port: not verified.
- NothingOS Wear firmware binary: not built.
- Physical installation: not performed.
- **Owner-device flashing remains blocked.**
