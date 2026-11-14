# Phone-only development path (iPhone owner)

The owner currently has an iPhone and no computer. An iPhone can be used to review the project, test the browser simulator, and access a cloud development environment. It cannot replace the watch's Android companion requirements or provide a safe physical firmware recovery path.

## What can be done from the iPhone now

1. Open the Watch Pro 2 web emulator: https://cmf.arnv.dev/emulator/watch-pro-2/
2. Explore the CMF Watch Pro 2 UI/device simulation and test sample firmware only inside the emulator.
3. Open upstream documentation: https://cmf.arnv.dev/
4. Use GitHub in Chrome to inspect the repository and, if available, create a Codespace for cloud-based development.
5. Keep the physical watch on stock firmware 1.0.0.73 while developing the interface and services.

## Codespaces setup

- Repository: `culprit-k/NothingOS-Watch`
- Branch: `main`
- Start with the smallest available Linux machine.
- The GitHub creation screen identifies which account pays for the Codespace. Review billing and spending limits before creating it.
- A Codespace is a cloud development environment; it does not give the iPhone direct access to watch debug pads, SWD, or a physical recovery interface.
- The repository currently has no validated D398 firmware build workflow. Do not assume that opening a Codespace means a device-ready firmware image can be built.

## iPhone and watch limitations

- iOS Safari/Chrome do not provide the Android Gadgetbridge firmware-installation route described by community projects.
- The official CMF iOS companion app is not a general-purpose custom firmware flasher.
- An iPhone can be used for browser-based development and UI validation, but a compatible Android test phone may eventually be needed for Android companion/BLE integration.
- A cloud runner can compile source, but it cannot physically recover a bricked watch.

## Firmware and OTA safety

Community tools report experimental BLE/OTA and custom firmware paths for the Watch Pro 2. Those projects are research references, not proof that an arbitrary image is compatible with this exact D398 device and stock firmware 1.0.0.73.

Before any OTA, the exact candidate image must be inspected and verified for:
- D398 board identity and hardware revision
- bootloader and partition compatibility
- secure-boot/signature requirements
- image format, CRC/checksums, and OTA metadata
- preservation of the original firmware and a demonstrated recovery route

A failed firmware update may require opening the watch and using internal SWD hardware recovery. A phone-side installer only transports bytes; it does not establish compatibility.

## Current next milestone

**P0: cloud workspace and emulator validation.**

- Confirm Codespaces availability and billing ownership before creation.
- Inspect the existing repository and establish a reproducible, non-device build environment.
- Validate the round-screen launcher, quick settings, notifications, health, media, and settings concepts in the browser emulator.
- Record which hardware features the emulator actually simulates.
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
