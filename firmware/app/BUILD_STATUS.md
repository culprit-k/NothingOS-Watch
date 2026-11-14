# Build and flash status

- Portable UI-state C module: strict GCC compile and unit tests pass in GitHub Actions.
- LVGL/Actions application source: authored, not yet verified by a full vendor SDK compile.
- SDK assembly script: added to overlay source into the vendor hello_world template.
- The script intentionally targets `ats3089c_dev_watch` for compile bring-up only.
- That board is not the CMF Watch Pro 2 D398 board.
- No `app.bin`, `sdfs.bin`, or D398 AOTA package is approved for installation.

## Physical D398 release blockers

1. Exact official stock OTA for D398 firmware 1.0.0.73.
2. Stock OTA inspection: board name, hardware version, partition manifest, image signatures/encryption, and resources.
3. Verified JX402 D398 board port, including display/touch/crown/PMIC pinmux and drivers.
4. Full vendor SDK build against that board port.
5. Resource generation and app-only AOTA replacement while preserving original boot/recovery.
6. Independent package validation and a proven recovery route.
7. Physical validation on a donor watch of the same hardware revision.

A green host-side unit test is not evidence that the full embedded app compiles or that a firmware image is safe to install.
