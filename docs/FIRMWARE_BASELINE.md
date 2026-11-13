# User device baseline

**Status:** recorded from user-provided Settings > About screenshot on 2026-11-13.

## Identifiers observed

| Field | Observed value | Interpretation |
|---|---|---|
| Product name / watch case | CMF Watch Pro 2 | User-confirmed |
| Watch About model | D398 | User-confirmed; matches the official product model identifier |
| Companion app device string | CMF Watch Pro 2-DB35 | User-confirmed app display string; relationship to D398 is not yet established |
| Installed firmware | 1.0.0.73 | User-confirmed |
| Firmware status | Up to date | As shown by the app at capture time |

Serial number and Bluetooth address are intentionally omitted. They are private device identifiers and are not needed for firmware compatibility research.

## Interpretation

D398 is the watch's published product model. DB35 is a separate identifier surfaced by the companion app. Do not treat DB35 as a PCB revision or firmware board name without evidence from the stock OTA metadata or hardware inspection.

## Compatibility research

- The community reCMF companion app reports real-device testing against Watch Pro 2 firmware 1.0.0.73 for pairing, health sync, notifications, weather, calls, alarms, and media. This supports protocol research on this firmware version; it does **not** establish custom-firmware compatibility.
- The public CMF Watch Pro 2 firmware/OTA research identifies an Actions ATS3089C target and discusses board-name/version gates, image checksums, optional boot-image signing, and OTA packaging.
- The exact stock OTA for this user's 1.0.0.73 build has not yet been inspected. Secure-boot/signature status, board_name, version_code, partition table, image format, and recovery state remain unknown.

## Next evidence needed

1. An exact official stock OTA package corresponding to version 1.0.0.73, obtained through the official app's normal update process or another lawful source.
2. Offline firmware metadata report: package hash, board_name, version_code, image inventory, per-image CRC result, compression, and signature presence.
3. Evidence that the custom image is built for the exact board/hardware revision and accepted by the stock update mechanism.
4. A verified recovery method on a donor device of the same hardware revision.

**No firmware write is authorized by this baseline record.**
