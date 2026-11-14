# Music app — NothingOS Wear

## Product requirement

The Music app has two distinct modes. They must not be confused or represented by the same demo state.

### 1. Phone music controls

Control audio already playing on the paired phone:
- Play and pause
- Previous and next track
- Volume adjustment only where the phone platform and media-session interface expose it
- Track title, artist, album artwork when available, playback state and elapsed time
- Clear disconnected, unavailable and permission-denied states

The owner's primary phone is an iPhone. iOS media-control and background Bluetooth behavior must be tested on the actual supported iOS versions. Do not claim Android MediaSession behavior is available on iOS. A later Android companion may be developed separately.

### 2. Music stored and played on the watch

The watch must be able to play locally stored audio without the phone acting as the audio source:
- Browse tracks and playlists stored in watch storage
- Play, pause, seek, previous and next
- Show track metadata and playback progress
- Support audio output through the watch speaker and/or paired Bluetooth audio devices, but only after the relevant D398 audio routes are confirmed
- Provide storage usage, import/transfer status, and safe handling of unsupported/corrupt files

The app must not display a working Play button until a real audio backend and output route are integrated. If the watch speaker or Bluetooth audio profile is unavailable to third-party firmware, expose only the output that has been verified.

## Architecture

- Music UI: LVGL screens for Phone, On-watch Library, Now Playing, Queue and Output.
- Phone control transport: versioned BLE commands and metadata events. Implement iOS compatibility as a separate tested milestone; do not assume Android APIs work on iPhone.
- Local library: indexed files in a verified writable storage area. Supported formats are determined by the actual decoder libraries and memory budget, not assumed in advance.
- Audio service: decoder, buffering, clock, playback state and output routing. This is a platform service, not a UI mock.
- Output drivers: D398 speaker amplifier path and Bluetooth audio profile, each independently verified.

## Required engineering gates

1. Inspect D398 firmware 1.0.0.73 and the vendor SDK for audio codec, speaker amplifier, storage, Bluetooth Classic/A2DP, BLE and audio output APIs.
2. Confirm available RAM, flash, filesystem layout and writable user-data partition.
3. Prove local file read and decode on the exact D398 target with a minimal audio test.
4. Prove an output route on a physical D398: speaker and/or paired Bluetooth headphones. Measure stability, latency, battery drain and thermal behavior.
5. Define and test file transfer from the iPhone. Do not assume iOS can send arbitrary files to a custom BLE service in the background.
6. Implement the local library and playback service; test corrupt files, low storage, interruptions, sleep/wake and recovery.
7. Separately implement phone media controls and test against iPhone playback, lock screen, app backgrounding and reconnect behavior.
8. Only integrate into a D398 firmware image after the board port, full SDK build, package validation and recovery gates are satisfied.

## Explicit non-claims

- The browser simulator is not an audio player.
- Phone controls do not mean audio is playing on the watch.
- Local playback is not complete until the watch itself decodes the file and produces audible output.
- A passing host-side C test is not evidence of D398 audio support.
- No firmware image is ready to flash based on this specification.

## Acceptance tests

- With the phone connected and a supported phone media session active, watch controls change the phone's playback state and metadata updates correctly.
- With the phone disconnected, a locally stored test track plays audibly from a verified watch output.
- Local playback continues according to the tested sleep/display policy and stops safely on low battery or storage errors.
- Reboot, reconnect and interrupted-transfer tests do not corrupt the local library.
