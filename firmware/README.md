# Firmware

**The original NothingOS Wear LVGL application source is now present in [app/](app/). No D398-ready firmware image exists yet.**

## Implemented source

- Actions/Zephyr application entry point and message loop
- LVGL home screen with explicit companion time-sync pending state
- Apps screen with unavailable modules labelled honestly
- System status screen
- UI navigation by available key/crown input and LVGL button events

The shell deliberately does not fabricate time, health, battery or connectivity readings.

## What remains before a device image

1. Acquire and inspect the owner's exact official stock OTA (D398, firmware 1.0.0.73).
2. Verify JX402 board identity, display/touch/input drivers, flash/partition layout and signature policy.
3. Complete the D398 board port and integrate the application with the correct vendor SDK and required stock support resources.
4. Compile and validate the app and matching SDFS resources.
5. Construct and independently verify the permitted app/resources-only OTA package.
6. Preserve stock rollback and prove recovery/boot on a donor D398 of the same hardware revision.

The public SDK's `ats3089c_dev_watch` development-board target is not a substitute for the watch's JX402 hardware. A successful development-board build must never be labelled D398-ready.

Do not add flashing scripts or publish device-ready binaries until the D398 board profile, image format, boot validation, stock backup and recovery gates in [docs/SAFETY.md](../docs/SAFETY.md) have been met.
