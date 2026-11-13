# External research and reuse policy

The following projects are independent sources. Read their current documentation, inspect exact revisions, and honor their licenses before adapting code.

| Source | What it contributes | Caution |
|---|---|---|
| [CMF-Open](https://github.com/TechJeeper/CMF-Open) | cmfkit protocol/simulator/firmware tooling, AOTA analysis, Zephyr/LVGL build research | Some documentation is for other models; confirm Watch Pro 2-specific evidence. |
| [Watch Pro 2 custom firmware research](https://github.com/krtkygpta/cmf-watch-2-pro-custom-firmware) | ATS3089C / `jx402_01_3089c` repacking and OTA experiments | Warns of internal SWD recovery risk. Its repacked binary is not NothingOS Wear. |
| [Watch Pro 2 firmware analysis](https://github.com/whatotter/cmf-watch-firmware) | Reverse engineering notes and stock image observations | Treat inferred facts as hypotheses until verified against our 1.0.0.73 stock image. |
| [CMF Watch Pro 2 BLE protocol](https://github.com/joshuapassos/CMF-Watch-Pro-2-BLE-Protocol) | BLE command and frame research | Some commands may differ or be inert on 1.0.0.73; use capture-backed evidence. |
| [CMF Watch Utils](https://github.com/arnav-kr/cmf-watch-utils) | Community post reports web/QEMU emulator and custom firmware OTA tooling | Verify latest repo code, release notes, and licenses before use. |
| [firmware-wp2-badapple](https://github.com/arnav-kr/firmware-wp2-badapple) | Experimental Zephyr demo firmware source | Demonstration firmware only; inspect license and code before reuse. |
| [reCMF](https://github.com/Bendy3192/reCMF) | Companion-app behavior tested by its author on Watch Pro 2 firmware 1.0.0.73 | Licensed AGPL-3.0-or-later; do not copy code without complying with its license. |

## Evidence standard

Mark each finding as one of:
- **Owner-confirmed** — visible on the owner's actual unit.
- **Hardware-verified** — tested on the exact model/revision with a recorded procedure.
- **Firmware-derived** — parsed from the exact stock firmware package.
- **Community-reported** — reported elsewhere but not reproduced by this project.
- **Unknown** — evidence insufficient.

A report for a generic ATS3089C development board, Watch 3 Pro, or another CMF model must not be presented as D398 compatibility.
