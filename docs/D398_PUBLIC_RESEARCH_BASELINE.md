# D398 public-research bring-up baseline

Status: research baseline only — **not a flash specification**.

This document records what can be prepared without a user-supplied stock OTA. It deliberately separates public community observations from facts verified on this individual watch.

## Known device target

- Product: CMF Watch Pro 2
- User-reported model: D398
- User-reported firmware: 1.0.0.73
- Candidate platform reported by public community firmware work: ATS3089C / `jx402_01_3089c`
- Candidate platform status: **unconfirmed for this exact hardware revision and firmware package**

The candidate platform string is a lead for source-level investigation, not permission to reuse a binary, board definition, partition map, or OTA package from another project.

## What can be developed without the stock OTA

1. Portable application logic and LVGL screens.
2. Host-side unit tests for state machines and navigation.
3. A Zephyr/Actions-SDK integration layer kept separate from device-specific drivers.
4. A D398 hardware abstraction interface with explicit TODOs for unverified peripherals.
5. Build and CI checks that compile portable code and reject accidental release packaging.
6. A simulator for UI iteration.
7. A reproducible evidence ledger for every hardware and boot-chain assumption.

## Hardware facts that must remain unresolved until independently verified

| Area | Current status | Required evidence |
|---|---|---|
| SoC / board revision | ATS3089C / jx402_01_3089c is a public-research candidate only | Device-identifying output or exact board documentation |
| Display controller and geometry | Unknown | Panel identification and verified driver configuration |
| Touch controller | Unknown | Controller identification, bus and interrupt mapping |
| Crown / buttons | Unknown | GPIO mapping and measured event behavior |
| PMIC / charging | Unknown | Exact IC and safe driver documentation |
| Sensors | Unknown per component | Exact part numbers, bus addresses and validation |
| Flash layout | Unknown | Verified device-specific partition map |
| Bootloader and image format | Unknown | Verified boot chain and package analysis |
| Signature / encryption policy | Unknown | Verified update acceptance requirements |
| Recovery path | Unknown | Tested recovery on a sacrificial device |

Do not fill these cells with values copied from another CMF model or an unverified community image.

## Development strategy

### Track A — host-verifiable software
Continue portable UI/state logic, unit tests, simulator work and strict CI. These deliverables can be completed without touching the watch.

### Track B — platform integration
Prepare interfaces for display, input, BLE, time, sensors, storage and audio. Implement a driver only when its exact hardware contract is supported by evidence. Unsupported functions must report unavailable rather than simulate success.

### Track C — device bring-up
Use the candidate ATS3089C platform only for source investigation and non-flashing build experiments. A generic development-board build is not a D398 firmware image.

### Track D — release engineering
Do not create or publish a flashable package until the exact board, partition layout, bootloader constraints, signing policy and recovery procedure are independently verified. Keep any proprietary firmware dump private; publish only hashes and redacted analysis.

## Music feature constraints

The product specification keeps two separate modes:
- Phone Music: remote control and metadata, subject to a verified iOS-compatible BLE protocol.
- Watch Music: local file storage, decoder, audio output route, controls and transfer path must all be verified on D398.

A visible button is not evidence of a working audio backend. Until the backend is implemented, the UI must show the feature as unavailable or experimental.

## Release rule

A successful host unit test or generic SDK build is not proof of D398 compatibility. No generated artifact from this research baseline is authorized for flashing.
