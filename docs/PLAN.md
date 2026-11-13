# NothingOS Wear — implementation plan

## Product definition
Build a **Wear OS-inspired embedded OS experience**, not a Google Wear OS port. The D398 is treated as an MCU-class wearable running a Zephyr-based firmware stack. Native watch apps are firmware components; Android APKs run on the paired phone.

## Phase 0 — Evidence baseline
- Record firmware version/build and hardware revision exposed by Settings/About.
- Separate Pro 2-specific findings from Watch 3 Pro or other CMF models.
- Obtain a stock OTA through the normal official update flow; preserve privately and record SHA-256.
- Inspect board name, version code, partitions, image format, and boot-image signature policy offline.
- Identify exact panel/touch driver, GPIO/pinmux, crown/button, haptic, sensors, and BLE behavior.
- Create a reproducible host development environment.

**Exit:** evidence table with source/confidence; no unverified pins or board identifiers in code; no personal data or stock binary committed.

## Phase 1 — Simulator-first system shell
- Define monochrome/dot-matrix design tokens and circular safe areas.
- Implement launcher, app list, quick settings, notification inbox, settings, and app lifecycle in a simulator.
- Abstract touch, crown rotation, crown press, and long press.
- Add deterministic clock/status mocks and UI state tests.
- Reuse LVGL UI code where practical.

**Exit:** all main screens render at 466 × 466; simulated touch/crown navigation works; tests cover wake/sleep, back, app launch/exit, and empty/error states.

## Phase 2 — Zephyr/LVGL build foundation
- Pin Actions SDK, Zephyr revision, compiler, Python, west dependencies, and build scripts.
- Reproduce hello-world for a supported ATS3089C development board.
- Keep development-board target distinct from D398.
- Add CI for formatting, host tests, build metadata, and hashes.
- Do not publish a D398 flashable image.

**Exit:** clean documented build, pinned dependencies, CI passing. A dev-board build is not proof of D398 compatibility.

## Phase 3 — D398 hardware bring-up
- Confirm board name and silicon from stock image/device evidence.
- Confirm memory map, linker regions, bootloader, partition layout, image header, version rules, and signature enforcement.
- Port display and touch from exact driver/pin data.
- Add crown/button, haptic, RTC, battery telemetry, and sensor services incrementally.
- Validate each driver in isolation.

**Exit:** exact board configuration evidence; display/input tests on a recoverable development unit; verified backup and recovery before writing to the owner's watch.

## Phase 4 — Android companion and BLE
Android app:
- Pairing/session management and BLE reconnect.
- Notification relay via Android Notification Listener permission.
- Media metadata/control relay via Android media-session APIs where supported.
- Time and selected phone-state sync.
- Settings/diagnostics and permission controls.

Watch:
- Verified BLE protocol integration.
- Notification inbox and media-control UI.
- Bounded queues, reconnect handling, and low-power radio policy.

**Exit:** pair/reconnect tests; safe handling of unsupported commands, denied permissions, and Bluetooth-off; no secrets or private notification payloads in logs.

## Phase 5 — Native watch apps
Initial apps: clock, timer, stopwatch, calculator, activity/health dashboard using verified sensors, weather relayed from phone, notifications, media controls, and device settings. These are native embedded components, not Android APKs installed on the watch.

## Phase 6 — Reliability and release
- Measure idle, display-on, BLE-connected, and sensor-active power.
- Implement sleep/wake and power-aware refresh.
- Stress-test BLE reconnect and notification bursts.
- Validate watchdog, storage integrity, and safe fallback.
- Publish supported hardware/firmware matrix, hashes, changelog, and known issues.
- Recovery-test on a nonessential donor unit.

**Release gate:** no stable label until hardware tests and recovery are repeatable. Publish source and reproducible build steps before binaries.

## First milestone
**M0: Simulator + reproducible toolchain.** Deliver a running UI shell and documented Zephyr build for a supported development target. It is not flashable D398 firmware.
