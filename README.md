# NothingOS Wear

**An original, Wear OS-inspired embedded operating system for the CMF Watch Pro 2 (D398).**

> **Status: embedded UI source now exists, but there is no NothingOS Wear firmware image ready to install.** This project does not port Google's Wear OS or Android. The target is a custom embedded OS using the watch's MCU-class platform, an LVGL interface and a companion-phone connection.

## Owner's device baseline

- Model: **CMF Watch Pro 2, D398**
- Installed stock firmware: **1.0.0.73**
- Owner's everyday phone: iPhone
- Development environment: GitHub Codespaces
- A temporary Android phone may be available for later BLE/OTA experiments.

## Try the interface safely — no coding or installation

1. Open the [Watch Pro 2 web emulator](https://cmf.arnv.dev/emulator/watch-pro-2/).
2. Choose the available demo firmware or UI simulation.
3. Explore the round-screen interface and sensor simulation.
4. This runs in the browser; it does not change the physical watch.

The emulator is a community research tool, not an official CMF service.

## What NothingOS Wear aims to include

- Round launcher and app grid
- Quick settings and notification centre
- Health/activity summaries with clear sensor-data provenance
- Music app with separate phone-control and on-watch local-playback modes
- Settings, display and haptics controls
- BLE companion integration
- Original visual language inspired by Nothing's minimal design

## Current engineering status

| Component | Status |
|---|---|
| Project architecture and roadmap | Documented |
| Browser UI concept | Present in `simulator/index.html` |
| Original Actions/Zephyr + LVGL application shell | Source added in `firmware/app/`; not yet compile-validated |
| Home, Apps and System UI pages | Source implemented |
| Crown/key page navigation | Source implemented; hardware input not yet validated on D398 |
| Companion phone, BLE, sensor and health integrations | Not implemented |
| Exact stock 1.0.0.73 OTA acquired and inspected | Not yet |
| Exact D398 board/bootloader/partition/signature profile | Not yet verified |
| Reproducible Actions Zephyr SDK build | Not yet established |
| D398-specific NothingOS Wear board port | Not built |
| NothingOS Wear OTA package | Does not exist yet |
| Physical installation and recovery test | Not performed |

## Why the watch has not been flashed

Public Watch Pro 2 firmware experiments provide useful development evidence, but they are not NothingOS Wear and do not establish safe installation on this exact D398/1.0.0.73 unit. A wrong image can leave the watch unbootable and may require internal SWD hardware recovery.

Before a physical installation, this project must:
1. Inspect the exact official stock OTA for firmware 1.0.0.73.
2. Confirm board identity, hardware revision, partition layout, update gates and secure-boot/signature policy.
3. Complete the JX402/D398-specific hardware port and integrate the original NothingOS Wear application.
4. Build the app and matching resources with the correct Actions SDK and vendor inputs.
5. Verify nested package structure, checksums and partition boundaries independently.
6. Preserve a verified stock rollback package and test recovery on a donor D398 of the same hardware revision.
7. Demonstrate physical boot and test display, touch, crown, BLE, charging, sensors, battery and sleep/wake.

**Do not install third-party demo firmware or a development-board build on the owner's only watch.** Android provides a possible BLE transfer route, not a compatibility guarantee or recovery mechanism.

## Repository guide

- [Project roadmap](docs/PLAN.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Music app specification](docs/MUSIC_APP_SPEC.md)
- [D398 device profile](docs/DEVICE_PROFILE.md)
- [Stock firmware baseline](docs/FIRMWARE_BASELINE.md)
- [Installation readiness](docs/INSTALLATION_READINESS.md)
- [Phone-only development path](docs/PHONE_ONLY_PATH.md)
- [Immediate installation audit](docs/D398_IMMEDIATE_INSTALL_AUDIT_2026-11-14.md)
- [External research references](docs/EXTERNAL_RESEARCH.md)
- `firmware/app/` — original embedded UI application source
- `companion/android/` — planned Android companion
- `simulator/` — browser UI concept

This is an independent community project and is not affiliated with Nothing or CMF.
