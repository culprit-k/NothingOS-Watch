# Nothing Watch Installer for Android

Phone-side package installer for CMF Watch Pro 2 (D398). This is separate from the NothingOS Wear firmware project.

## Android app 0.2.0

The Android source now implements the phone-side package path:

- Selects a `.wp2app` package and validates its 64-byte WP2A header, ABI, exact file length, header/body CRCs and reserved bytes.
- Scans for and connects to a nearby CMF Watch Pro 2 over LE GATT.
- Enables the shell, command and data notifications, then reads `AT GETSECRET`.
- Keeps the 16-byte channel key in memory only; it is cleared on disconnect and never logged or persisted.
- Opens the authenticated K app channel with AES-128-CBC and CRC32 framing.
- Probes INFO slot `0xFE` to determine whether a compatible watch-side app stub is present.
- Inspects the app/loader slots, selects a free slot, and asks before replacing an app with the same name.
- Sends INFO/BEGIN/DATA/END, checks every DATA offset acknowledgement, then reads INFO back to verify the installed header.
- Negotiates a larger ATT MTU before transferring; safely stops if the phone/watch cannot negotiate at least 158 bytes. No package payload is sent before that check.

The implementation has source-level tests for the wire format and package-header parsing. A successful Android CI build and a physical test on the owner's D398 are still release gates; do not treat source code as a tested APK.

## Important watch-side prerequisite

**This installer does not flash firmware.** The public [fmctools/cmf-watch-sdk](https://github.com/fmctools/cmf-watch-sdk) explains that its app packages require a compatible watch-side stub in the patched app firmware and a separate loader package. The stock Watch Pro 2 firmware does not contain that app stub. On stock firmware, the app-channel probe is expected to stop without transferring a package.

The project root's D398 baseline currently records stock firmware 1.0.0.73 and says NothingOS Wear has no firmware image ready to install. This Android installer does not turn that research build into NothingOS Wear, and it must not be used as a reason to flash a third-party firmware image onto the owner's only watch.

The one-time watch-side firmware preparation, its exact hardware compatibility, recovery path and physical verification remain separate unresolved tasks. No OTA image is bundled here.

## Build

GitHub Actions builds the debug APK using JDK 17 and Gradle 8.9. See the repository Actions tab and the **Android Watch Installer** workflow for its artifact. Only use an APK after the workflow succeeds and the artifact has been checked.

## Safety boundaries

- No firmware OTA or bootloader operation.
- No factory reset, Bluetooth unpairing or bond deletion.
- No slot DELETE command.
- No automatic overwrite: an existing same-name package requires an explicit confirmation.
- No automatic replacement of loader or other slots.
- No claim of physical compatibility until tested on the exact D398 hardware.
