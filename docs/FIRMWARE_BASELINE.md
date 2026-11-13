# User device baseline

**Status:** recorded from user-provided Settings > About screenshot on 2026-11-13.

## Confirmed from the screenshots

| Field | Observation |
|---|---|
| Product name | CMF Watch Pro 2 |
| Watch model | D398 |
| Installed firmware | **1.0.0.73** |
| Firmware status | Up to date at screenshot time |
| Companion-app device name | Appends a four-hex suffix to the product name |

The companion-app suffix in the screenshot matches the final two bytes of the Bluetooth address shown on the same screen. It is therefore likely the BLE advertising/device-name suffix, not a second hardware model or board revision. The suffix, full Bluetooth address, and serial number are not reproduced here.

## Compatibility research

- The community reCMF companion app reports real-device testing against Watch Pro 2 firmware 1.0.0.73 for pairing, health sync, notifications, weather, calls, alarms, and media. This supports **companion-protocol** research for this firmware version; it does not establish custom-firmware compatibility.
- The public CMF Watch Pro 2 BLE protocol document reports reverse engineering from firmware 1.0.0.73 and live captures on a real Watch Pro 2.
- A Pro 2-specific custom-firmware research project identifies Actions ATS3089C and board `jx402_01_3089c`, and documents AOTA packaging/OTA transfer experiments. It warns that recovery can require opening the watch and using internal SWD pads.
- The exact stock OTA for this user's 1.0.0.73 build has not been inspected. Secure-boot/signature status, board_name, version_code, partition table, image format, and recovery state remain unknown.

## Next evidence needed

1. Exact official stock OTA package corresponding to version 1.0.0.73.
2. Offline firmware metadata report: package hash, board_name, version_code, image inventory, per-image CRC, compression, and signature presence.
3. Evidence that a candidate build targets the exact board/hardware revision and is accepted by its stock update mechanism.
4. A verified recovery method on a donor device of the same hardware revision.

**No firmware write is authorized by this baseline record.**
