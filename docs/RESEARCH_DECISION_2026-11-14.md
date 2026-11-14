# NothingOS Wear — firmware research decision (2026-11-14)

## Objective

Replace the stock CMF Watch Pro 2 (D398) operating firmware with an original, Wear OS-inspired embedded interface called NothingOS Wear. This is not a port of Google's Android/Wear OS.

## Confirmed owner baseline

- Product model: CMF Watch Pro 2, D398.
- Owner-reported stock firmware: 1.0.0.73.
- Development host available: GitHub Codespaces in iPhone Chrome.
- A temporary Android phone may become available for later BLE testing.
- No donor watch or SWD recovery equipment has been confirmed.

## Best current technical foundation

Community reverse-engineering identifies the Watch Pro 2 platform as Actions ATS3089C, using an Actions Zephyr 2.7 SDK, LVGL, and an AOTA-style firmware container. A public experimental Zephyr firmware project and a web emulator are available as research references. These are not a NothingOS Wear release.

Research references:
- https://github.com/arnav-kr/firmware-wp2-badapple
- https://github.com/arnav-kr/cmf-watch-utils
- https://cmf.arnv.dev/emulator/watch-pro-2/
- https://github.com/whatotter/cmf-watch-firmware
- https://github.com/joshuapassos/CMF-Watch-Pro-2-BLE-Protocol
- https://github.com/krtkygpta/cmf-watch-2-pro-custom-firmware

All third-party code must be reviewed for license and provenance before copying or adapting. Do not copy proprietary stock firmware or prebuilt binaries into this repository.

## Architecture decision

- Firmware runtime: Actions Zephyr SDK, pending exact-version and board verification.
- UI: LVGL, round 466 × 466 display target as reported by community research.
- Application model: monolithic firmware application; do not assume Android APK support or a watch app loader.
- Phone integration: BLE companion protocol, initially read-only and tested on a spare Android phone.
- Host simulation: community Watch Pro 2 emulator and this repository's browser UI simulator.

## Implementation sequence

### Phase 0 — establish source and hardware facts
- Obtain the exact official OTA package corresponding to owner firmware 1.0.0.73 through a legitimate owner-controlled update/download path.
- Inspect metadata, board_name, version_code, partition table, image CRCs, and whether boot images are signed.
- Compare the stock app image against the ATS3089C SDK's supported display, touch, sensor, and pinmux drivers.
- Record all findings with hashes and provenance. No flash operation.

### Phase 1 — reproducible development build
- Pin an Actions SDK revision and toolchain.
- Build an unmodified SDK sample for its documented development board.
- Run static checks and emulator tests.
- Label the result development-board-only; it is not a D398 image.

### Phase 2 — NothingOS Wear shell
- Implement boot shell, launcher, app grid, quick settings, notifications, settings, health summary, and media controls in LVGL.
- Keep sensor values marked simulated until verified on hardware.
- Add UI tests and simulator screenshots.

### Phase 3 — D398 board adaptation
- Derive panel/touch/controller and GPIO mappings from the exact stock app and trusted hardware evidence.
- Create a separate D398 board definition only after those values are supported by evidence.
- Validate boot, display, touch, power, charging, BLE, sensors, and recovery on a donor device.

### Phase 4 — package and installation validation
- Preserve stock bootloader, recovery, and resources.
- Determine signature enforcement from the exact stock OTA; do not assume it is disabled.
- Validate OTA container metadata, board identity, version gate, and checksums.
- Demonstrate rollback and recovery on a donor D398 before considering the owner's watch.

## Explicit no-flash gate

Do not produce or distribute a purported D398-ready OTA until all of these are true:
1. Exact D398 board identity and hardware revision are confirmed.
2. Stock firmware 1.0.0.73 has been inspected and hashed.
3. Boot signature policy and OTA acceptance rules are known.
4. Candidate image and package pass independent structural checks.
5. A recovery path has been demonstrated on a donor device of the same hardware revision.
6. Core functions have been tested and limitations documented.

A failed update may require opening the watch and internal SWD hardware recovery. An Android phone can transfer an image; it cannot prove that the image is compatible or recover a bricked watch.

## Current status

- Research plan: recorded.
- Existing UI concept simulator: present.
- Exact stock OTA inspection: not done.
- Zephyr SDK build: not done.
- D398 board port: not done.
- NothingOS Wear firmware image: does not exist yet.
- Physical installation: not safe to attempt.
