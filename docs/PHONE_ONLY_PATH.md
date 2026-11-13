# Phone-only development and installation path

The owner currently has an Android phone but no computer. Use browser-based simulation and phone-side protocol exploration first. A phone cannot replace the Actions Zephyr build environment.

## What can be done from the phone now

1. Open the Watch Pro 2 web emulator: https://cmf.arnv.dev/emulator/watch-pro-2/
2. Explore the CMF Watch Pro 2 UI/device simulation and test sample firmware only inside the emulator.
3. Open upstream documentation: https://cmf.arnv.dev/
4. Keep the physical watch on stock firmware 1.0.0.73 while developing our own interface and services.
5. Use GitHub mobile/web to review and edit this repository. Substantial firmware builds require a Linux computer or cloud development environment.

## Android companion / BLE research

- Community tools report direct BLE support and an experimental firmware installer path for CMF Watch Pro 2.
- Gadgetbridge can be considered for read-only pairing/protocol diagnostics, but do not start a firmware transfer yet.
- Before any OTA, the exact target image must be inspected and verified for D398 board identity, stock bootloader compatibility, signatures, image CRCs, and recovery. An arbitrary binary or image from another model is not safe.
- The experimental custom Zephyr Bad Apple image is a demo, not NothingOS Wear. It is not a feature-complete replacement and must not be treated as our installable build.

## Why not flash immediately from Android?

A phone-side installer only transports bytes. It does not prove the firmware image is compatible. Community documentation warns that a failed image can leave the watch unbootable and require opening the case and SWD hardware recovery. The existing public custom-firmware guide also uses a particular repacked image; that is not a release of this project.

## What requires a computer or cloud build runner

- Building the Actions Zephyr SDK and compiling firmware.
- Running image inspection/repacking tools and verifying package metadata.
- Creating reproducible build artifacts and checksums.
- Emulator/QEMU builds and automated firmware tests.
- Recovery tooling and SWD validation.

A cloud runner can compile source, but it cannot provide physical recovery if the watch bricks.

## Phone-only next milestone

**P0: emulator and UX validation.**
- Validate round-screen navigation and screen layouts.
- Decide launcher, quick settings, notifications, health, and settings behavior.
- Record what hardware features the emulator actually simulates.
- Keep physical device actions read-only until a compatible image and tested recovery path exist.

## Installation release checklist

- [ ] Exact stock 1.0.0.73 OTA package inspected.
- [ ] Exact D398 board name and hardware revision confirmed.
- [ ] Candidate app image built for matching board/pin configuration.
- [ ] Secure boot/signature state confirmed.
- [ ] Package and image checksums validated.
- [ ] Original bootloader/recovery partitions preserved.
- [ ] Official OTA replay/rollback tested on a donor unit.
- [ ] Recovery route demonstrated on same hardware revision.
- [ ] Candidate image tested in emulator and on a nonessential donor watch.

**Current decision: no physical firmware transfer.** This is a technical safety gate, not a requirement to buy a computer before doing useful work.
