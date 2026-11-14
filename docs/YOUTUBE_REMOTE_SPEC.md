# YouTube on NothingOS Wear — feature specification

## User request

Add a YouTube experience to NothingOS Wear for the CMF Watch Pro 2 (D398).

## Product decision

Build **YouTube Remote** first: a small watch app that controls YouTube playing on the paired phone. Do not promise native video playback on the watch.

The D398 is an MCU-based smartwatch platform with a 466 × 360 display and BLE phone connection. The available model-specific protocol research documents BLE command/data channels, but does not establish Wi-Fi video streaming, a supported YouTube client runtime, or a video decoder/audio pipeline. The watch therefore cannot be treated like an Android Wear device.

## First release: YouTube Remote

Watch UI:
- Now Playing: video title, channel name, thumbnail (when the phone companion can provide it), playback state and progress.
- Controls: play/pause, previous/next where supported, seek back/forward, volume where the phone OS exposes it.
- Queue: show a short list of items supplied by the phone.
- Launch on phone: ask the companion to open the selected YouTube video on the phone.
- Connection state: clearly show when the phone is disconnected.

Phone companion:
- Android-first implementation using Android MediaSession / notification media controls to observe and control playback where permitted by the active YouTube app and Android version.
- Use Android intents/deep links to open YouTube videos on the phone.
- Send compact metadata and button events over BLE.
- No scraping, credential collection, unofficial account login, or bypass of YouTube ads/DRM.
- iPhone support is a later investigation; iOS background and Bluetooth restrictions must be tested before promising parity.

## Not in the first release

- No video playback/rendering on the watch.
- No audio streaming to the watch.
- No YouTube sign-in or account management on the watch.
- No claim of official YouTube app status or Google certification.

## Why native video is not a realistic initial target

YouTube video playback requires a supported network path, codecs, sustained throughput, memory, audio output and a supported application/runtime stack. BLE is used here as a low-bandwidth companion link; it is not a practical substitute for Wi-Fi video delivery. The exact D398 firmware/SDK and hardware capabilities still need verification. A thumbnail and playback-control interface is a much more credible feature.

## Engineering milestones

1. Implement a UI-only YouTube Remote mock screen in the browser simulator.
2. Define a versioned, compact BLE message contract for metadata and controls.
3. Build an Android companion prototype using public Android media-session APIs and YouTube deep links.
4. Validate BLE transport against a physical D398 running stock 1.0.0.73 before integrating into custom firmware.
5. Port the watch screen into the D398-specific NothingOS Wear application after the board port is validated.
6. Test play/pause, seek, metadata refresh, reconnect, permission denial, phone lock and app termination.
7. Keep all controls visibly disabled when the phone companion or playback session is unavailable.

## Safety and platform note

This feature specification does not authorize firmware flashing. The custom OS still has the same independent D398 board, stock OTA, secure-boot, rollback and physical recovery blockers listed in the main installation-readiness document.
