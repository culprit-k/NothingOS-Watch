# Firmware

**The original NothingOS Wear LVGL application source is present in app/. No D398-ready firmware image exists yet.**

## Implemented source

- Actions/Zephyr application entry point and message loop using the public SDK template API pattern.
- LVGL Home, Apps, System and Timer pages.
- Five-minute countdown timer screen with Start, Pause, Resume and Reset controls.
- Portable monotonic countdown service with wraparound handling and host tests.
- Portable UI state model and navigation tests.
- Separate Phone Music and Watch Music service contract with unavailable-by-default state and host tests.
- Honest status labels for integrations that do not yet have a verified backend.

The timer uses the platform monotonic uptime clock; it does not need a wall-clock time sync. The Music service does not bind a real phone or local-audio backend yet. No health, battery, connectivity, or track data is fabricated.

## What remains before a device image

1. Obtain complete authorized Actions SDK dependencies and toolchains; the public SDK manifest references internal vendor SSH repositories.
2. Verify JX402/D398 board identity, display/touch/input drivers, flash/partition layout and signature policy.
3. Complete the D398 board port and integrate the application with the correct vendor SDK and required stock support resources.
4. Compile and validate the full embedded app and matching SDFS resources for an identified development target, then a verified D398 target.
5. Construct and independently verify the permitted app/resources-only OTA package.
6. Preserve stock rollback and prove recovery/boot on a donor D398 of the same hardware revision.

The public SDK ATS3089C development-board target is not a substitute for the watch's JX402 hardware. A successful development-board build must never be labelled D398-ready.

Do not add flashing scripts or publish device-ready binaries until the D398 board profile, image format, boot validation, stock backup and recovery gates in docs/SAFETY.md have been met.
