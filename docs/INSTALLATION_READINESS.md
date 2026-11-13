# Installation readiness — CMF Watch Pro 2 D398

## Current decision

**DO NOT INSTALL NothingOS Wear on the user's watch yet.**

The repository currently contains documentation and a browser-based UI concept simulator. It does not contain a tested D398 firmware image, a verified rollback package, or a demonstrated recovery path for this exact unit.

The user's watch is confirmed to report firmware **1.0.0.73**. The exact stock OTA for that version and its boot-image signature status have not been examined.

## Why a custom firmware path exists

Community work now exists for:
- Actions ATS3089C / Zephyr-based Watch Pro 2 firmware experiments.
- Custom firmware build and AOTA repacking research.
- BLE OTA transport and companion tooling.
- A Watch Pro 2 web/QEMU emulator and an experimental Zephyr demo firmware.

This is meaningful progress, but it is not equivalent to a stable general-purpose alternative OS. A demonstration firmware is not NothingOS Wear and does not automatically preserve health, BLE, notifications, sensors, or battery functions.

## Risk

A published Watch Pro 2 custom-firmware guide warns that the watch has no normal external USB recovery mode. A corrupted or incompatible image may require opening the case and connecting a hardware debugger/programmer to internal SWD pads. This can cause permanent loss of the watch if the procedure or hardware assumptions are wrong.

The existence of an OTA transfer method does not prove an image is valid for this exact unit. The stock bootloader can enforce board identity, version, checksums, and possibly image signatures.

## Mandatory gates before installing anything

- [ ] Obtain and preserve the exact official 1.0.0.73 stock OTA package.
- [ ] Compute and record SHA-256; never modify the preserved original.
- [ ] Parse OTA metadata and verify board_name matches the running device.
- [ ] Verify image inventory, compression, CRCs, version_code, and partition descriptors.
- [ ] Inspect boot image headers and determine whether app images are signed.
- [ ] Confirm the SDK build output targets the exact watch board, not just a generic ATS3089C development board.
- [ ] Confirm display, touch, crown, haptic, sensor, PMU, flash, and BLE hardware configuration.
- [ ] Establish a real recovery route and test it on a nonessential donor watch of the same revision.
- [ ] Build and test NothingOS Wear in simulator/emulator before any physical installation.
- [ ] Validate BLE and low-power behavior with a disposable development unit.

If any gate fails or remains unknown, do not flash the user's watch.

## Safe next actions

1. Keep the watch on official firmware 1.0.0.73.
2. Use the browser simulator for UI design and interaction.
3. Use community firmware/emulator sources for local simulation only after reviewing provenance and license.
4. If the user has access to an Android phone, evaluate an established companion app as a separate, reversible software experiment; it does not replace the watch OS.
5. Continue building our own UI and system services, with the physical target disabled until the board profile is proven.

## Not included

There is deliberately no "one-click flash" button, OTA proxy script, raw firmware image, or SWD wiring diagram in this repository at this stage.
