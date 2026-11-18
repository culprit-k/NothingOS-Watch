# CMF Watch Pro 2 installer protocol port

This implementation is based on the public `fmctools/cmf-watch-sdk` source (`wp2apps/link.py`, `install.py`, `blegatt.py`, `package.py`).

## Implemented in the Android prototype

- WP2A package header/body CRC validation (64-byte header).
- Frame header encoding and streaming notification parser.
- AES-128-CBC / PKCS padding and CRC32 payload integrity.
- Installer command IDs and little-endian BEGIN/DATA payload encoders.
- ATT long-write policy must use GATT write-with-response whenever a frame exceeds MTU minus 3; split write commands are not protocol-compatible.

## Not implemented / not yet physically validated

- Android GATT service discovery and reliable notification subscription lifecycle.
- Reading the watch's local shell secret and opening the authenticated app channel.
- Request/response correlation, retries, timeouts, reconnect handling.
- INFO slot inspection, free-slot selection, BEGIN/DATA/END installation and post-install INFO verification.
- Real D398 test against the user's watch.

The app must not expose secret material in UI, logs, analytics, crash reports, or persistent storage. The installer must never invoke DELETE or firmware OTA operations in the initial release. It must verify an existing app slot before replacement and stop safely on every unexpected response.
