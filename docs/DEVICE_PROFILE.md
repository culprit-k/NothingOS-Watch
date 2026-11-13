# Device profile — CMF Watch Pro 2

## Owner-confirmed
- Product shown on watch About screen: CMF Watch Pro 2.
- Watch model shown: D398.
- Firmware version shown by companion app: 1.0.0.73 (up to date at screenshot time).
- The companion app displays a device name with a four-hex suffix. In the screenshot this suffix matches the final two bytes of the displayed Bluetooth address, so it is most likely the BLE advertising/device-name suffix, **not** a second hardware model or PCB revision. The suffix and full address are deliberately not stored here.

## Platform — community-reported; verify on exact unit
- Actions ATS3089C.
- Zephyr RTOS 2.7 and LVGL.
- A Pro 2-specific community firmware guide identifies board `jx402_01_3089c`.
- Exact board name, panel/touch drivers, pinmux, sensors, flash map, bootloader, signatures, and recovery remain to be verified from this unit's exact stock OTA.

See [FIRMWARE_BASELINE.md](FIRMWARE_BASELINE.md) and [INSTALLATION_READINESS.md](INSTALLATION_READINESS.md).

Serial number and Bluetooth address from the user's screenshots are intentionally not stored in the repository.
