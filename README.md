# NothingOS Wear

**Wear OS-inspired custom firmware and Android companion ecosystem for CMF Watch Pro 2 (D398).**

> **Status: research and bring-up. Not an installable firmware release.** This project does not port Google's Wear OS or Android to the watch. The target is a custom embedded operating environment based on the watch's MCU-class hardware, with a Wear OS-inspired UI and Android-phone companion integration.

## Current target baseline

- Watch model: D398 (owner-confirmed)
- Companion app model string: CMF Watch Pro 2-DB35 (recorded separately; exact meaning unknown)
- Installed stock firmware: **1.0.0.73** (owner-confirmed)
- Community-reported platform: Actions ATS3089C, Zephyr RTOS 2.7, LVGL.

## Vision

Circular launcher, cards, notifications, quick settings, settings, native embedded apps, BLE connection to Android companion, phone-relayed notifications/media, and verified health/sensor views.

## Reality check

| Goal | Feasibility |
|---|---|
| Wear OS-inspired interface | Project goal |
| Custom Zephyr/LVGL firmware | Research path; exact D398 board port required |
| Android companion over BLE | Project goal; protocol implementation required |
| Phone-relayed notifications/media | Conditional on Android permissions and verified protocol |
| Install APKs directly on the watch | Not supported by this MCU/Zephyr architecture |
| Actual Google Wear OS / Play Services | Not a realistic port target for this hardware |

## Current implementation

- Project architecture and phased roadmap.
- Browser UI concept simulator in `simulator/index.html`.
- Device baseline and explicit installation safety gates.

**NothingOS Wear is not currently installable on the physical watch.** Do not flash experimental images.

## Repository map

- `docs/PLAN.md` — phased roadmap
- `docs/ARCHITECTURE.md` — system architecture
- `docs/SAFETY.md` — safety policy
- `docs/DEVICE_PROFILE.md` — D398 profile
- `docs/FIRMWARE_BASELINE.md` — firmware 1.0.0.73 baseline
- `docs/INSTALLATION_READINESS.md` — pre-installation gates
- `docs/EXTERNAL_RESEARCH.md` — upstream research and reuse policy
- `firmware/` — future Zephyr/LVGL firmware
- `companion/android/` — future Android companion
- `simulator/` — UI simulator

## Research starting points

See [docs/EXTERNAL_RESEARCH.md](docs/EXTERNAL_RESEARCH.md) for relevant Watch Pro 2 firmware, BLE, simulator, and companion-app projects. Review licenses and exact model/firmware coverage before reusing code.

## Safety

A failed firmware update may require internal SWD hardware recovery. See [docs/SAFETY.md](docs/SAFETY.md) and [docs/INSTALLATION_READINESS.md](docs/INSTALLATION_READINESS.md). This project is unofficial and not affiliated with Nothing or CMF.
