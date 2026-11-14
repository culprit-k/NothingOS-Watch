# NothingOS Wear — implementation plan

## Product definition
Build a **Wear OS-inspired embedded OS experience**, not a Google Wear OS port. The D398 is treated as an MCU-class wearable running a Zephyr-based firmware stack. Native watch apps are firmware components; Android APKs run on the paired phone.

## Phase 0 — Evidence baseline
- Record firmware version/build and hardware revision exposed by Settings/About.
- Separate Pro 2-specific findings from Watch 3 Pro or other CMF models.
- Preserve and inspect only an exact official stock OTA when it becomes available through an authorized source.
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

**Exit:** main screens render in the round-screen safe area; simulated navigation works; tests cover page navigation, wake/sleep and empty/error states.

## Phase 2 — Portable app services
- Implement and test service logic independently from device drivers.
- Deliver a countdown timer using monotonic time, with pause/resume/reset and wraparound handling.
- Keep music phone-control and local-playback backends separate and unavailable until real platform services are connected.
- Add host-side CI for all portable service logic.

**Exit:** service unit tests pass with strict compiler warnings; UI reports unavailable for unimplemented hardware services.

## Phase 3 — Zephyr/LVGL build foundation
- Pin Actions SDK, Zephyr revision, compiler, Python, west dependencies, and build scripts.
- Reproduce hello-world for a supported ATS3089C development board.
- Obtain the full authorized vendor SDK dependency tree; the public manifest alone refers to private SSH repositories.
- Keep development-board target distinct from D398.
- Do not publish a D398 flashable image.

**Exit:** clean documented full SDK build, pinned dependencies, CI passing. A dev-board build is not proof of D398 compatibility.

## Phase 4 — D398 hardware bring-up
- Confirm board name and silicon from exact stock image/device evidence.
- Confirm memory map, linker regions, bootloader, partition layout, image header, version rules, and signature enforcement.
- Port display and touch from exact driver/pin data.
- Add crown/button, haptic, RTC, battery telemetry, and sensor services incrementally.
- Validate each driver in isolation.

**Exit:** exact board configuration evidence; display/input tests on a recoverable development unit; verified backup and recovery before writing to the owner's watch.

## Phase 5 — Phone companion and BLE
The user's primary phone is an iPhone. Do not assume Android-only APIs satisfy the iPhone experience.

- Define and test the exact watch BLE services and protocol before writing phone integrations.
- Evaluate iOS notification and media-control limitations with a real iPhone.
- Build platform-specific companion apps only for APIs that can be exercised and verified.
- Add pairing/session management, reconnect handling, permissions and privacy-safe logging.
- Use Android Notification Listener and MediaSession APIs only as a separately scoped Android companion capability.

**Exit:** pairing/reconnect tests; safe handling of unsupported commands, denied permissions, and Bluetooth-off; no secrets or private notification payloads in logs.

## Phase 6 — Native watch apps
Initial apps: clock after verified RTC/time sync, timer, stopwatch, calculator, activity/health dashboard using verified sensors, weather relayed from phone, notifications, media controls, and device settings. These are native embedded components, not Android APKs installed on the watch.

## Phase 7 — Reliability and release
- Measure idle, display-on, BLE-connected, and sensor-active power.
- Implement sleep/wake and power-aware refresh.
- Stress-test BLE reconnect and notification bursts.
- Validate watchdog, storage integrity, and safe fallback.
- Publish supported hardware/firmware matrix, hashes, changelog, and known issues.
- Recovery-test on a nonessential donor unit.

**Release gate:** no stable label until hardware tests and recovery are repeatable. Publish source and reproducible build steps before binaries.

## Current verified milestone
**Portable source milestone:** UI state, two-mode music contract and countdown timer logic compile under host GCC with strict warnings and pass their unit tests in GitHub Actions. The embedded LVGL app and vendor SDK have not yet been compiled as a full firmware build. It is not flashable D398 firmware.
