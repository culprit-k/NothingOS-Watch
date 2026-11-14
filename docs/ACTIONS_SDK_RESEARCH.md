# Actions SDK findings for NothingOS Wear

Research date: 2026-11-14

## Source inspected and pin

Public repository: https://github.com/lvgl/lv_port_actions_technology

The SDK's current public master ref at the time of this audit resolved to:

- Commit: `26f51e584940d5b141cd1d09d2629d69b595f579`
- Repository: `lvgl/lv_port_actions_technology`

The SDK describes itself as an LVGL port for Actions Technology ATS30xx evaluation boards. Its README states the SDK is beta and API behavior may change. Its example workflow is built around evaluation boards and a host-side configuration tool.

The public tree includes:
- `hello_world/`: minimal Zephyr/LVGL template application.
- `action_technology_sdk/application/app_demo/local_music_demo/`: local music demo.
- `action_technology_sdk/application/app_demo/bt_music_demo/`: Bluetooth music demo.
- `action_technology_sdk/zephyr/boards/arm/ats3089c_dev_watch/`: generic ATS3089C development-watch board definition.
- `hello_world/boards/ats3089c_dev_watch.conf`: template overlay for that generic board.

## Music APIs discovered

The local music demo's source uses Actions media-player, local-player, file-stream and filesystem services. It has a local directory abstraction and control entry points for play/pause, next/previous and volume. It is coupled to the vendor media and filesystem services, not just LVGL.

The Bluetooth music demo uses the Actions Bluetooth manager, A2DP, AVRCP, audio policy and media-player stack. The sample accepts a Bluetooth audio stream, decodes SBC/AAC, and exposes play/pause/next/previous and volume handling plus track metadata and playback-position callbacks.

These are real SDK examples and useful integration references. They do not prove that the D398 has the same amplifier, speaker route, storage layout, filesystem mount, Bluetooth role/profile support, RAM budget, or enabled Kconfig options. In particular, the CMF companion app's BLE connection is not automatically the same as a Bluetooth Classic A2DP/AVRCP audio connection.

See [MUSIC_BACKEND_AUDIT.md](MUSIC_BACKEND_AUDIT.md) for the source-level analysis and current NothingOS service boundary.

## A confirmed limitation in the public source build

The public SDK repository's `action_technology_sdk/zephyr/west.yml` manifest lists many critical dependencies through SSH URLs on an internal host such as `192.168.4.4:29418`. These include Actions framework, media/audio, Bluetooth, display, sensor, filesystem and application repositories.

That means a public GitHub checkout alone does not provide the full vendor dependency tree needed for a complete Zephyr build. Without authorized access to the vendor repositories (or a complete redistributable SDK bundle), the full firmware cannot be reliably built from the public tree. The public README's example build also requires a Zephyr toolchain; final firmware/resource configuration uses a Windows-only vendor configuration tool.

This is a concrete blocker, not a missing C source file. The repository records it rather than pretending a template copy is a finished firmware build.

## Development-board caution

The generic `ats3089c_dev_watch` board definition is present in the SDK. Its defconfig contains concrete evaluation-board assumptions, including:
- panel driver selection for ICNA3310B;
- CST820 touch-controller configuration;
- optional/selected audio amplifier configuration;
- specific external battery supply and sensor Kconfig choices;
- flash, SRAM and PSRAM memory regions.

Those settings belong to the generic Actions evaluation board. They are not verified D398 hardware facts. None of them may be copied into a D398 target profile without independent device evidence.

The SDK README documents a 466 × 466 smartwatch evaluation-board case. That number is not by itself confirmation of the D398 display controller or pixel geometry.

## Repository integration

The NothingOS Wear assembly script:
- pins the public Actions SDK to commit `26f51e584940d5b141cd1d09d2629d69b595f579`;
- copies the minimal `hello_world` template;
- explicitly writes its CMake source list (including the portable music service);
- sets the generic ATS3089C development board only for later compile bring-up;
- never produces or authorizes a D398 flashable package.

The current GitHub Actions workflow compiles and tests portable source modules with host GCC and checks the assembly script's shell syntax. It does not claim to compile the vendor SDK or generate a watch image.

## Remaining target-specific work

1. Obtain an authorized complete Actions SDK/dependency bundle or vendor access so the real Zephyr build can run.
2. Confirm the exact D398 board identity, hardware revision, display/touch/crown, PMIC, sensors, storage, audio and BLE setup independently.
3. Implement a D398 board port without borrowing unverified development-board wiring.
4. Integrate and test real backends one by one, keeping unsupported features unavailable.
5. Keep the D398 flash gate closed until exact package/signature/recovery requirements have been verified and a donor-device recovery test succeeds.

## References

- [Actions Technology LVGL SDK](https://github.com/lvgl/lv_port_actions_technology/tree/26f51e584940d5b141cd1d09d2629d69b595f579)
- [Local music demo at the pinned SDK revision](https://github.com/lvgl/lv_port_actions_technology/tree/26f51e584940d5b141cd1d09d2629d69b595f579/action_technology_sdk/application/app_demo/local_music_demo)
- [Bluetooth music demo at the pinned SDK revision](https://github.com/lvgl/lv_port_actions_technology/tree/26f51e584940d5b141cd1d09d2629d69b595f579/action_technology_sdk/application/app_demo/bt_music_demo)
- [Generic ATS3089C development-watch board definition](https://github.com/lvgl/lv_port_actions_technology/tree/26f51e584940d5b141cd1d09d2629d69b595f579/action_technology_sdk/zephyr/boards/arm/ats3089c_dev_watch)
- [Generic ATS3089C template overlay](https://github.com/lvgl/lv_port_actions_technology/blob/26f51e584940d5b141cd1d09d2629d69b595f579/hello_world/boards/ats3089c_dev_watch.conf)
