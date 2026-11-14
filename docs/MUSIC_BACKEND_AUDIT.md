# Music backend audit

Research date: 2026-11-14

## Two different user features

NothingOS Wear keeps these as separate backends:

1. **Phone Music** — the watch controls music on a paired phone, requests play/pause/next/previous, and receives track, playback-position and volume updates when the phone exposes them.
2. **Watch Music** — music files stored on the watch are decoded and played through an output route that the actual board supports.

Neither mode may be shown as connected or playing until the backend reports a real state event.

## Public Actions SDK evidence

The public Actions LVGL/Zephyr SDK contains two separate demo apps.

### Bluetooth music demo

Files inspected:
- action_technology_sdk/application/app_demo/bt_music_demo/prj.conf
- src/bt_player/bt_player.c and bt_player.h
- src/main/system_app_main.c

The sample enables Bluetooth Classic, A2DP, AVRCP, audio/media playback and a platform audio output path. Its player:
- accepts an A2DP stream from a connected phone;
- opens the Actions media player with the negotiated SBC/AAC codec;
- routes decoded audio to the platform output;
- handles AVRCP playback status, track changes, ID3 metadata and playback position;
- issues AVRCP play, pause, next and previous commands;
- synchronizes volume.

This is a genuine technical starting point for a phone-to-watch audio route and media controls. It is not proof that D398 exposes the same profiles, routes audio to its speaker, or accepts the same Bluetooth configuration. The CMF/iPhone BLE companion pairing is not automatically the same as this Bluetooth Classic A2DP/AVRCP connection.

### Local music demo

Files inspected:
- action_technology_sdk/application/app_demo/local_music_demo/prj.conf
- src/lcmusic/lcmusic_app.c and lcmusic.h
- src/main/system_app_main.c

The sample uses the Actions media player, local-player and file-stream facilities, filesystem services and decoder/parser modules. Its default directory is CONFIG_APP_FAT_DISK (shown as /SD: in the sample configuration), and the source includes support paths for SD, USB, NOR and NAND volumes.

This confirms a vendor local-file playback stack exists. It does not establish that D398 has a user-accessible, writable music partition, the correct filesystem mount, available decoder resources, or a supported speaker/headphone route. Copying this full demo into the watch app before those facts are known would be misleading and could consume substantial memory and resources.

## Current implementation in this repository

firmware/app/src/music_service.{h,c} provides a portable, testable contract for the two independent modes. It:
- keeps Phone Music and Watch Music state separate;
- defaults both modes to UNAVAILABLE;
- requires an explicit backend binding before a command can run;
- requires the backend to report a connected/ready state before controls are accepted;
- does not claim that play/pause changed playback until a real backend event updates the state;
- uses a 0–100 abstract volume percentage that a vendor backend must translate to its native range.

There is intentionally no physical audio backend bound yet. The module is an integration boundary, not a player implementation.

## Remaining target-specific work

Before enabling either backend on D398, the target port must verify:
- Bluetooth Classic role/profile support, pairing behavior, A2DP/AVRCP interoperability with iOS, and whether AVRCP can control the user's chosen iPhone music app;
- audio output route, amplifier/codec driver, speaker limits and audio policy;
- storage device, mounted filesystem, writable partition, safe transfer mechanism, file formats, and available memory;
- required Actions Kconfig, DSP images, resource files and boot-time services;
- battery/sleep behavior during audio playback.

Until those checks pass, both backends remain reported as unavailable in the real device build.
