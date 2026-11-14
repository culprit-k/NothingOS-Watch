# Actions SDK findings for NothingOS Wear

Research date: 2026-11-14

## Source inspected

Public repository: https://github.com/lvgl/lv_port_actions_technology

The SDK repository describes itself as an LVGL port for Actions Technology ATS30xx evaluation boards. Its README states the SDK is beta and API behavior may change. Its example workflow is built around evaluation boards and a host-side configuration tool.

The public tree includes:
- `hello_world/`: minimal Zephyr/LVGL template application.
- `action_technology_sdk/application/app_demo/local_music_demo/`: local music demo.
- `action_technology_sdk/application/app_demo/bt_music_demo/`: Bluetooth music demo.
- `hello_world/boards/ats3089c_dev_watch.conf`: a generic ATS3089C development-watch board configuration.

## Important music discovery

The local music demo readme describes playing locally stored songs, with console commands for play, stop, next and previous.

The Bluetooth music demo readme describes playback from a connected Bluetooth source through the device speaker, with play/stop/previous/next and volume controls.

This is stronger evidence than a speculative audio design: the public SDK contains separate sample applications for local and Bluetooth audio paths. However, these are SDK examples, not proof that the CMF Watch Pro 2 D398 has the same audio amplifier, speaker route, storage mapping, Bluetooth profile support, RAM budget or enabled Kconfig options.

Do not copy the demos wholesale into NothingOS Wear until their source APIs, configuration dependencies, resource requirements and target-board assumptions are audited.

## Build process findings

The SDK README describes:
1. Copying the `hello_world` template into `action_technology_sdk/application/app_demo`.
2. Sourcing the SDK's Zephyr environment.
3. Running the SDK build script.
4. Selecting a board, application and sub-application.
5. Using a Windows-only configuration tool for firmware configuration and resource packaging.

The README's documented examples use evaluation boards. Its generic `ats3089c_dev_watch` is not identified as the CMF Watch Pro 2 D398 board. Therefore the public SDK build path is useful for source/API bring-up only, not for producing an image to flash to the user's watch.

## Repository action taken

The NothingOS Wear SDK assembly script was corrected to explicitly write its source list. The previous string substitution could have inserted `app_msg.c` twice because that file already appears in the upstream template's source list.

The script remains explicitly marked as development-board-only and non-flashable.

## Next implementation steps

1. Audit the local music demo source and its Kconfig dependencies.
2. Audit the Bluetooth music demo source and determine whether it is A2DP sink, another profile, or a vendor-specific route.
3. Identify which audio APIs are common platform services and which are board-specific.
4. Keep the NothingOS Music service behind an interface until D398 speaker, storage and Bluetooth capabilities are verified.
5. Add the first actual audio backend only after an exact target configuration can be built and tested.

## References

- Actions Technology LVGL SDK: https://github.com/lvgl/lv_port_actions_technology
- Local music demo directory: https://github.com/lvgl/lv_port_actions_technology/tree/master/action_technology_sdk/application/app_demo/local_music_demo
- Bluetooth music demo directory: https://github.com/lvgl/lv_port_actions_technology/tree/master/action_technology_sdk/application/app_demo/bt_music_demo
- Generic ATS3089C development-watch config: https://github.com/lvgl/lv_port_actions_technology/blob/master/hello_world/boards/ats3089c_dev_watch.conf
