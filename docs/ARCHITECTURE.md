# Proposed architecture

## System boundary
NothingOS Wear has two cooperating products:
1. **Watch firmware:** Zephyr RTOS + LVGL built for verified D398 hardware.
2. **Phone companion:** a platform-specific app providing BLE session management and relaying supported information. An Android build and an iOS build have different operating-system APIs and must not be treated as interchangeable.

Android does not run on the watch. The watch has no Android APK runtime or Google Play Services.

## Watch firmware layers
```text
UI: Launcher | Apps | Notifications | Settings
Native apps: Timer | Health | Media | Activity
Services: monotonic timer | wall-clock sync | health | media | alerts | power
BLE protocol | storage | sensor abstraction
Zephyr RTOS | LVGL | board support
verified MCU | display | touch | crown | sensors | PMU
```

## Android companion layers
```text
Pairing | Dashboard | Permission explanation
Notification listener | MediaSession relay
BLE session | Protocol codec | Reconnect
Android Bluetooth APIs and OS permissions
```

## iPhone integration boundary
The user's primary phone is iPhone. Do not claim that Android Notification Listener or Android MediaSession APIs provide iOS behavior. iOS notification forwarding, background BLE operation, and media-control behavior require separate implementation and physical-device testing. If an iOS API does not expose the required capability, the UI must show it as unsupported rather than simulating success.

## Core interfaces
- `InputEvent`: touch coordinates, crown delta/click, back/long press.
- `UiState`: screen, focus, modal, scroll position, wake/sleep.
- `WatchStatus`: battery, time, connection, sensor availability.
- `NotificationMessage`: bounded title/body/source/timestamp; never log private content.
- `MediaState`: title/artist/playback state and supported controls.
- `SensorSample`: type, value, unit, timestamp, quality/availability.
- `BleTransport`: connect, disconnect, send, receive, timeout, error.
- `TimerService`: monotonic millisecond source, countdown duration, remaining time and state.

The current portable TimerService handles countdown state only; it does not depend on a wall clock. The current MusicService holds independent phone/local backends and refuses commands until a backend reports readiness. Neither has a D398 hardware integration yet.

Protocol encoding and encryption must follow verified device-specific documentation. Do not invent UUIDs, keys, opcodes, or packet layouts.

## Resource constraints
Use fixed-size buffers and bounded queues; avoid dynamic allocation in hot UI/BLE paths; keep assets compact; limit polling; make display and radio power-aware; do not assume Android-class CPU, RAM, storage, or GPU resources.

## Security and privacy
Never commit pairing secrets/session keys. Validate BLE lengths and inputs. Request only necessary phone permissions. Keep notification content local. Hash and inspect firmware packages before any update flow.
