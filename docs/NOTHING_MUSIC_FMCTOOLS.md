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
