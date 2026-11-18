# Nothing Watch Installer for Android

This is the phone-side companion for the CMF Watch Pro 2 (D398), built separately from the watch application.

## Prototype 0.1.0

- Selects a `.wp2app` package through Android's document picker.
- Validates WP2A magic, ABI, package length, and header/body CRC fields locally.
- Scans for a nearby device advertising as CMF Watch Pro 2.
- Does **not** connect, authenticate, transfer, delete, or install anything yet.

## Installation protocol requirements

The verified community SDK uses a BLE GATT session, shell-secret retrieval, AES-CBC/CRC framing, session authentication, and the WP2A INFO/BEGIN/DATA/END command sequence. A plain BLE write is not compatible. These stages must be ported and tested before enabling installation.

The APK is built by GitHub Actions. See the repository's Actions tab and the `Android Watch Installer` workflow for the debug APK artifact.

Never confirm unexpected pairing/binding prompts or attempt firmware flashing as part of this app. This is an app-package installer prototype, not a firmware flasher.
