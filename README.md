# NothingOS Wear

**Wear OS-inspired custom firmware and companion ecosystem for CMF Watch Pro 2 (D398).**

> **Status: research and bring-up. Not an installable firmware release.** This project does not port Google's Wear OS or Android to the watch. The target is a custom embedded operating environment built around the watch's existing MCU-class hardware, with a Wear OS-inspired interface and Android-phone companion integration.

## Vision
- Circular launcher, cards, notifications, quick settings, and settings.
- Native embedded watch applications.
- BLE connection to an Android companion app.
- Phone-relayed notifications and media controls where supported.
- Time, activity, sensor, and battery views using verified hardware support.

## Target
- Device: CMF Watch Pro 2, model D398 (owner-confirmed).
- Community-reported platform: Actions ATS3089C, Zephyr RTOS 2.7, LVGL, 466 × 466 round display.
- Exact board, panel/touch controller, firmware build, flash layout, and boot security must be verified before target-specific builds or updates.

## Reality check
| Goal | Feasibility |
|---|---|
| Wear OS-inspired interface | Project goal |
| Custom Zephyr/LVGL firmware | Research path; exact board port required |
| Android companion over BLE | Project goal; protocol work required |
| Phone-relayed notifications/media | Conditional on Android permissions and verified protocol |
| Install APKs on the watch | Not supported by this MCU/Zephyr architecture |
| Actual Google Wear OS / Play Services | Not a realistic port target for this hardware |

## Repository map
- `docs/PLAN.md` — phased roadmap and acceptance criteria
- `docs/ARCHITECTURE.md` — system architecture
- `docs/SAFETY.md` — flashing and recovery gates
- `docs/DEVICE_PROFILE.md` — evidence-based D398 profile
- `firmware/` — future Zephyr/LVGL firmware
- `companion/android/` — future Android companion
- `protocol/` — BLE notes and sanitized fixtures
- `simulator/` — host-side UI/service simulation

## Roadmap
1. Verify device identity, stock firmware version, and board profile.
2. Reproduce community build/emulator workflow without flashing.
3. Build simulator-first UI shell and navigation.
4. Develop BLE transport and Android companion.
5. Port only verified drivers to a D398-specific target.
6. Add applications, notifications, media, and health views.
7. Validate power, stability, update integrity, and recovery.

## Research starting points
- [CMF-Open](https://github.com/TechJeeper/CMF-Open)
- [CMF Watch Pro 2 custom firmware research](https://github.com/krtkygpta/cmf-watch-2-pro-custom-firmware)
- [CMF Watch Pro 2 firmware dump](https://github.com/whatotter/cmf-watch-firmware)
- [CMF Watch Pro 2 BLE Protocol](https://github.com/joshuapassos/CMF-Watch-Pro-2-BLE-Protocol)
- [CMF Watch Utils](https://github.com/arnav-kr/cmf-watch-utils)

Review each project's current status and license before reusing code. NothingOS Wear is unofficial and not affiliated with Nothing or CMF. See [docs/SAFETY.md](docs/SAFETY.md).
