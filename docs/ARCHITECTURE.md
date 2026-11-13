# Proposed architecture

## System boundary
NothingOS Wear has two cooperating products:
1. **Watch firmware:** Zephyr RTOS + LVGL built for verified D398 hardware.
2. **Android companion:** an ordinary phone app providing BLE session management and relaying information.

Android does not run on the watch. The watch has no Android APK runtime or Google Play Services.

## Watch firmware layers
```text
UI: Launcher | Cards | Notifications | Settings
Native apps and screen/state lifecycle
Services: clock | health | media | alerts | power
BLE protocol | storage | sensor abstraction
Zephyr RTOS | LVGL | board support
ATS3089C | display | touch | crown | sensors | PMU
```

## Android companion layers
```text
Pairing | Dashboard | Permission explanation
Notification listener | MediaSession relay
BLE session | Protocol codec | Reconnect
Android Bluetooth APIs and OS permissions
```

## Core interfaces
- `InputEvent`: touch coordinates, crown delta/click, back/long press.
- `UiState`: screen, focus, modal, scroll position, wake/sleep.
- `WatchStatus`: battery, time, connection, sensor availability.
- `NotificationMessage`: bounded title/body/source/timestamp; never log private content.
- `MediaState`: title/artist/playback state and supported controls.
- `SensorSample`: type, value, unit, timestamp, quality/availability.
- `BleTransport`: connect, disconnect, send, receive, timeout, error.

Protocol encoding and encryption must follow verified device-specific documentation. Do not invent UUIDs, keys, opcodes, or packet layouts.

## Resource constraints
Use fixed-size buffers and bounded queues; avoid dynamic allocation in hot UI/BLE paths; keep assets compact; limit polling; make display and radio power-aware; do not assume Android-class CPU, RAM, storage, or GPU resources.

## Security and privacy
Never commit pairing secrets/session keys. Validate BLE lengths and inputs. Request only necessary phone permissions. Keep notification content local. Hash and inspect firmware packages before any update flow.
