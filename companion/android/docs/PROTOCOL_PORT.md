# CMF Watch Pro 2 installer protocol port

Protocol behavior is ported from the public [fmctools/cmf-watch-sdk](https://github.com/fmctools/cmf-watch-sdk), especially `src/wp2apps/link.py`, `install.py`, `blegatt.py` and `package.py`.

## Implemented in Android source

- WP2A package validation: 64-byte header, ABI/kind, exact package length, header/body CRC32 and zeroed reserved bytes.
- INFO header parsing and verification for an installed slot.
- BLE LE GATT connection, service/characteristic lookup and notification subscription.
- Shell `AT GETSECRET` retrieval and memory-only 16-byte K channel key.
- Authenticated AES-128-CBC / PKCS padding / CRC32 framing and notification reassembly/filtering.
- INFO slot probing, including the stub-version query at slot `0xFE`.
- Free-slot selection; same-name replacement is held for explicit user confirmation.
- BEGIN/DATA/END transfer with per-chunk offset acknowledgements and post-install INFO verification.
- No DELETE, firmware OTA, factory reset, unpair or bond-management implementation.

## Android transport constraint

The largest encrypted DATA frame is 155 bytes (11-byte protocol header plus 144-byte AES body). Android first requests a larger ATT MTU and this build refuses to start package transfer unless the negotiated MTU is at least 158 bytes. This avoids splitting a protocol frame into incompatible write commands. The exact negotiated MTU and physical behavior still need validation on the owner's Samsung S23 FE and D398.

## Watch prerequisite

The upstream SDK's own documentation distinguishes stock firmware (no app stub), a patched firmware carrying the stub, and the separately installed loader. A K-channel reply alone is not a substitute for that complete supported setup. This Android project intentionally implements only the app-package transport; it does not build or apply the watch firmware patch or install an unverified OTA.

## Not yet physically validated

- Android LE connection and shared-link behavior while the watch is bonded to the iPhone.
- GATT notifications, GETSECRET and K-channel requests on the actual D398.
- Negotiated MTU and long-duration transfer against the real watch.
- Installing and launching an app on hardware after the required compatible stub and loader are present.

The app must never expose the channel key in UI, logs, analytics, crash reports or persistent storage. A successful package transfer is not proof that the watch can launch the package.
