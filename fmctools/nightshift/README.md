# NIGHTSHIFT — CMF Watch Pro 2 D398

A native, multi-panel experimental app for the JX402 CMF Watch Pro 2. It is an app package, not replacement firmware.

## Panels in v0.1

- **CORE** — local watch clock/date, page navigation and status.
- **PULSE** — requests the watch heart-rate service and displays HEART events. This is a spot display, not a medical measurement or diagnosis.
- **FOCUS** — 25-minute focus timer with start/pause and reset.
- **SIGNAL** — sends an SOS haptic pattern (three short, three long, three short) when tapped.
- **SYSTEM** — app/ABI information and interaction hints.

Crown rotates between panels. Tap the main action area to activate the panel. Crown press/right swipe exits to the custom-app menu. A stock popup pauses the app and stops haptics.

## Hardware boundaries

- Uses only the documented app ABI: 466×466 RGB565 drawing, touch, crown, local RTC, heart-rate request/events and vibration.
- Does not claim access to GPS, speaker/audio playback, Wi-Fi, NFC, camera, arbitrary Android notifications, or battery percentage.
- Heart-rate availability and accuracy depend on the stock firmware, loader, sensor state and wrist contact. Not a medical device.
- Haptic SOS is only a local vibration demo; it does not transmit an emergency message.
- No phone companion is required for v0.1.

## Build

Run `bash scripts/build-nightshift-app.sh`. The workflow builds/tests against the pinned FMC community apps SDK and uploads `nightshift.wp2app` as a GitHub Actions artifact.

## Install safety

This build reports ABI 1.3 and therefore requires the matching ABI 1.3 loader and compatible stub-enabled firmware (the SDK's current release identifies stub version 5 for JX402 / stock 1.0.0.73). It cannot be installed on untouched stock firmware through the normal CMF app. Do not flash any firmware image unless the exact D398 firmware/board match and recovery path have been verified. The app package itself is installed over BLE through the loader and does not replace stock firmware. See the SDK's versioned release and OTA notes before attempting any device write.
