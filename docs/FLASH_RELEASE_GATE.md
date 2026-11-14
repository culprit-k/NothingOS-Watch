# Flash release gate — D398 / firmware 1.0.0.73

**Release decision: BLOCKED. No image in this repository is approved for the owner's watch.**

This document is the single release checklist. A check is complete only when the referenced evidence is committed or linked privately and independently reviewed. A green host test or development-board build never satisfies a device gate.

## Evidence required from the exact watch

- [ ] Exact official stock OTA corresponding to D398 firmware 1.0.0.73 obtained from the owner's own update/cache path. Keep the original private and unmodified; do not commit proprietary stock firmware.
- [ ] SHA-256, file size, package format, AOTA metadata, board_name, hw_ver, version_code, image inventory, CRCs and partition descriptors recorded in a redacted report.
- [ ] Boot-image headers inspected; signature and secure-boot policy established from the actual stock package. Do not infer this from another CMF model or a generic SDK example.
- [ ] Board identity and hardware revision confirmed; `jx402_01_3089c` remains a community-reported candidate until verified from the exact package/device.
- [ ] Exact panel, touch, crown/key, PMU, haptic, sensor, storage and BLE configuration established from stock evidence.

## Build requirements

- [ ] Pin the exact Actions SDK commit, Zephyr revision, compiler and dependency versions.
- [ ] Build the original NothingOS Wear app against a D398-specific board definition, not `ats3089c_dev_watch`.
- [ ] Compile the complete embedded application and link successfully; portable host unit tests are supplementary only.
- [ ] Validate the generated app image header, memory regions, resources and runtime configuration against the stock D398 layout.
- [ ] Validate every app feature against actual hardware interfaces. Unimplemented services must remain disabled and clearly labelled.

## Package requirements

- [ ] Preserve the untouched stock package privately as rollback evidence.
- [ ] Replace only the permitted application/resource partition(s); preserve bootloader, recovery and unrelated stock partitions.
- [ ] Validate package/container CRCs, nested image checksums, lengths, offsets, compression, metadata and version rules with an independent parser.
- [ ] Confirm the stock boot chain accepts unsigned/custom application images. If a required signature is unavailable, stop; do not bypass secure boot or suggest key replacement.
- [ ] Produce a signed-off manifest with source commit, SDK commit, tool versions, package SHA-256 and validation report.

## Recovery and physical validation

- [ ] Establish a practical recovery method for this exact hardware revision.
- [ ] Test rollback and recovery on a nonessential donor D398 of the same hardware revision before touching the owner's watch.
- [ ] Validate boot, display, touch, crown, BLE, notifications, audio, sensors, charging, battery reporting, sleep/wake, watchdog and storage integrity.
- [ ] Confirm a known-good stock restore package and recovery instructions are available locally before any write.

## Stop conditions

Stop and do not flash if any required evidence is missing, the board or version does not match, signatures cannot be satisfied through the normal supported update path, the package parser reports any discrepancy, or same-revision recovery has not been demonstrated. Never rename a generic development-board image or third-party demo into a D398 release.

## Current evidence snapshot

- Device: CMF Watch Pro 2 D398.
- Installed firmware reported by owner: 1.0.0.73.
- Exact stock OTA: **not supplied or inspected**.
- D398 board port: **not complete/verified**.
- Full D398 SDK build: **not completed**.
- NothingOS Wear OTA package: **does not exist**.
- Same-revision donor recovery test: **not performed**.
- Flash decision: **BLOCKED**.

## Research references

- [CMF-Open custom firmware research](https://github.com/TechJeeper/CMF-Open/blob/main/docs/04-custom-firmware.md) describes the need to inspect the actual stock OTA for board identity, partitions and signatures. Some of its model discussion is for Watch 3 Pro; do not transfer model-specific assumptions to D398 without evidence.
- [Watch Pro 2 custom firmware research](https://github.com/krtkygpta/cmf-watch-2-pro-custom-firmware) identifies ATS3089C / `jx402_01_3089c` and warns of SWD-level recovery risk. Its repacked image is not NothingOS Wear and is not approved for this device.
- [Watch Pro 2 firmware dump](https://github.com/whatotter/cmf-watch-firmware) is a community research artifact; the repository does not establish that its dump is the owner's exact 1.0.0.73 OTA. Do not use it as rollback evidence.
