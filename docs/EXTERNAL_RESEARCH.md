# External research and reuse policy

These are independent community projects. Review current revisions, source provenance, and licenses before reuse.

| Source | Contribution | Caution |
|---|---|---|
| [CMF-Open](https://github.com/TechJeeper/CMF-Open) | Actions SDK, Zephyr/LVGL, firmware/OTA analysis | Some pages cover other models; confirm Pro 2 applicability. |
| [Watch Pro 2 custom firmware research](https://github.com/krtkygpta/cmf-watch-2-pro-custom-firmware) | ATS3089C / jx402_01_3089c repacking and OTA experiments | Warns of internal SWD recovery risk; its repacked binary is not NothingOS Wear. |
| [Watch Pro 2 firmware analysis](https://github.com/whatotter/cmf-watch-firmware) | Firmware dump and structure research | Not proof that a particular image matches the owner's 1.0.0.73 unit. |
| [Watch Pro 2 BLE protocol](https://github.com/joshuapassos/CMF-Watch-Pro-2-BLE-Protocol) | BLE protocol captures, including a device on 1.0.0.73 | Community reverse-engineering; validate each command. |
| [CMF Watch Utils](https://github.com/arnav-kr/cmf-watch-utils) | Phone/PC tools and Watch Pro 2 web/QEMU emulator | Treat firmware upload/OTA functions as experimental. |
| [Experimental Bad Apple firmware](https://github.com/arnav-kr/firmware-wp2-badapple) | Open Zephyr demo firmware source | Demonstration only, not a complete smartwatch OS or NothingOS Wear release. |
| [reCMF](https://github.com/Bendy3192/reCMF) | Companion-app behavior reported on Watch Pro 2 firmware 1.0.0.73 | AGPL-3.0-or-later; comply with license before reuse. |

## Evidence standard

Mark each finding:
- **Owner-confirmed** — visible on the owner's unit.
- **Hardware-verified** — tested on the exact model/revision.
- **Firmware-derived** — parsed from the owner's exact stock OTA.
- **Community-reported** — reported elsewhere, not reproduced here.
- **Unknown** — evidence insufficient.

A generic ATS3089C dev-board, Watch 3 Pro, or other CMF model report is not proof of D398 compatibility.
