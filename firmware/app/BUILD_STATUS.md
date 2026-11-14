# Build and flash status

## Source and test status

- Portable UI-state C module: strict GCC compile and unit tests pass in GitHub Actions.
- Portable Music service contract: strict GCC compile and unit tests pass; backends remain deliberately unbound.
- Portable countdown Timer service: strict GCC compile and unit tests pass, including pause/resume, expiration and 32-bit uptime wrap.
- LVGL/Actions application source: authored with Home, Apps, System and Timer pages; not yet verified by a full vendor SDK compile.
- SDK assembly script: pinned to the public Actions SDK template and passes shell syntax checks.
- Public SDK's west manifest contains private vendor SSH dependencies, so a public GitHub checkout alone is not enough for a complete SDK build.
- The script target ats3089c_dev_watch is only a generic compile bring-up target, not the CMF Watch Pro 2 D398 board.
- No app.bin, sdfs.bin, or D398 AOTA package is approved for installation.

## Physical D398 release blockers

1. Exact D398 hardware revision, board identity and stock boot-chain evidence.
2. Exact official stock OTA for D398 firmware 1.0.0.73, with board name, hardware version, partition manifest, image signatures/encryption and resources inspected.
3. Verified JX402 D398 board port, including display/touch/crown/PMIC pinmux and drivers.
4. Authorized full vendor SDK dependencies and reproducible full SDK build.
5. Resource generation and app-only AOTA replacement while preserving original boot/recovery.
6. Independent package validation and a proven recovery route.
7. Physical validation on a donor watch of the same hardware revision.

A green host-side unit test is not evidence that the full embedded app compiles or that a firmware image is safe to install.
