# Android companion app

This directory is reserved for an Android application running on the paired phone, not on the watch.

Planned responsibilities:
- BLE discovery, pairing, session, and reconnect.
- Notification relay through Android Notification Listener permission, enabled explicitly by the user.
- Media metadata/control relay through Android media-session APIs where supported.
- Time and selected phone-state sync.
- Device status and diagnostics.

First document verified D398 BLE services, characteristics, and protocol behavior. Do not invent UUIDs, opcodes, keys, or packet layouts.

Explain notification access before requesting it. Do not upload notification content. Do not log message bodies, pairing secrets, or identifiers. Provide per-feature toggles and disconnect/forget-device controls.
