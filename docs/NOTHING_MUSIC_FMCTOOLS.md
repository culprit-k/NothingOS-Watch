# Nothing Music — fmctools implementation

This is a separate Rust app target alongside the existing NothingOS Wear Zephyr/LVGL project.

## SDK facts

- ABI 1.1 provides 128-byte host messages and 4 KiB app storage.
- ABI 1.3 adds phone media events, Api::media_key and Api::media_bind.
- Media-key commands are play, pause, previous and next. The documented interface has no volume command.
- The app ABI has no documented audio decoder or speaker-output function.

## Feature status

| Feature | Status |
|---|---|
| Rust watch app source | Added |
| Round Now Playing UI | Added |
| Play/pause/previous/next | SDK API wired; physical D398/iPhone test pending |
| Phone/playback status | ABI 1.3 event wired; physical test pending |
| Metadata and cover thumbnail protocol | Added; phone host bridge pending |
| Volume | Not exposed by current media-key API |
| Standalone audio decoding | Not implemented |
| Watch speaker output | Not implemented; D398 route unverified |
| iOS companion | Not implemented; no global artwork access assumed |

## Audio research

The public Actions Technology SDK has local-music and Bluetooth A2DP/AVRCP demo applications. They are useful source references but target generic Actions evaluation boards. The D398 amplifier, speaker route, Bluetooth roles, writable storage and memory configuration need independent verification. The public vendor SDK manifest also references private SSH dependencies, preventing a complete reproducible build from the public checkout alone.

No firmware or loader update is authorized by this source change. Keep the watch on stock firmware until loader and device compatibility are verified.

## Additional speaker-path research

The community repository fmctools/firmware-wp2-badapple contains a separate Zephyr firmware-level player for the Watch Pro 2. Its source describes mono 16 kHz MP3 decoding, a 32 KiB PCM ring buffer and playback through the DAC/PMUDAC speaker route. Its validation record reports QEMU/package validation but explicitly says physical flashing and sustained playback remain unverified. This is evidence that a firmware-level audio path has been developed, not evidence that the Rust app-loader ABI exposes that path.

Therefore the complete product remains split:
- Rust app-loader app: phone controls, metadata/artwork display and touch UI.
- NothingOS Zephyr firmware service: local-file decoding and speaker output, ported only after the D398 audio path and SDK dependencies are verified.

Reference: https://github.com/fmctools/firmware-wp2-badapple
