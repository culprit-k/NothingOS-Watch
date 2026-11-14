# Installation readiness — CMF Watch Pro 2 D398

## Current decision

**DO NOT INSTALL NothingOS Wear on the user's watch yet.**

The repository now contains:
- an original Actions/Zephyr + LVGL application shell in C source;
- Home, Apps, System and Timer pages;
- a five-minute countdown timer UI and portable monotonic timer service;
- portable UI state and two-mode Music service modules;
- host-side unit tests and GitHub Actions checks;
- a browser UI simulator and hardware/firmware research documentation.

It still does **not** contain a completed vendor-SDK firmware build, a D398-specific board port, a tested D398 firmware image, a verified rollback package, or a demonstrated recovery path for this exact unit.

The user's watch is confirmed to report firmware **1.0.0.73**. The exact stock OTA for that version and its boot-image signature status have not been examined. The user has said they do not have the stock OTA; this project is continuing with public source research and does not treat that missing file as permission to guess device-critical values.

## Build status

- Portable UI state, Music service contract and countdown timer logic compile with host GCC using strict warnings and pass the current GitHub Actions tests.
- SDK source assembly is pinned to a public Actions SDK revision and its shell script passes syntax validation.
- The embedded LVGL application source has not yet been built against a complete Actions/Zephyr SDK.
- The public SDK's west manifest points essential Actions dependencies to private SSH repositories on an internal host. A public GitHub checkout alone does not close those dependencies.
- The selected `ats3089c_dev_watch` target is a generic evaluation board, not a verified D398 target.

A green host unit-test run proves only the portable C modules. It does not prove that the embedded UI links, boots or runs on either the generic evaluation board or the CMF watch.

## Why a custom firmware path exists

Community work now exists for:
- Actions ATS3089C / Zephyr-based Watch Pro 2 firmware experiments.
- Custom firmware build and AOTA repacking research.
- BLE OTA transport and companion tooling.
- A Watch Pro 2 web/QEMU emulator and an experimental Zephyr demo firmware.

This is meaningful research, but it is not equivalent to a stable general-purpose alternative OS. A demonstration firmware is not NothingOS Wear and does not automatically preserve health, BLE, notifications, sensors, or battery functions.

## Risk

Public Watch Pro 2 custom-firmware research warns that normal external USB recovery may not be available. A corrupted or incompatible image may require opening the case and connecting a hardware debugger/programmer to internal SWD pads. This can cause permanent loss of the watch if the procedure or hardware assumptions are wrong.

The existence of an OTA transfer method does not prove an image is valid for this exact unit. The stock bootloader can enforce board identity, version, checksums, and possibly image signatures.

## Mandatory gates before installing anything

- [ ] Exact D398 hardware revision and board identity confirmed.
- [ ] Exact official 1.0.0.73 stock OTA parsed, SHA-256 recorded, and original preserved privately.
- [ ] OTA metadata verified, including board_name, version_code, image inventory, compression, CRCs and partition descriptors.
- [ ] Boot image headers inspected and signature/secure-boot policy established.
- [ ] Authorized complete SDK dependencies and toolchains available.
- [ ] Full embedded app compiled and linked for a verified D398 board definition.
- [ ] Display, touch, crown, haptic, sensor, PMU, flash, audio and BLE configuration confirmed from exact-device evidence.
- [ ] Package structure, permitted partitions and checksums independently verified.
- [ ] A real recovery route tested on a nonessential donor watch of the same hardware revision.
- [ ] Physical validation completed for boot, display, touch, inputs, BLE, charging, battery, sensors, sleep/wake and storage.

If any gate fails or remains unknown, do not flash the user's watch.

## Safe current use

1. Keep the physical watch on official firmware 1.0.0.73.
2. Use the browser simulator to explore the UI concept; it does not change the watch.
3. Continue portable source development and tests without generating a D398 package.
4. Treat vendor demo builds and community firmware images as research artifacts only, never as NothingOS Wear releases.

## Not included

There is deliberately no one-click flash button, raw firmware image, OTA proxy, or SWD wiring procedure in this repository at this stage. The D398 target manifest retains `flash_authorized: false` and all release gates remain closed.
