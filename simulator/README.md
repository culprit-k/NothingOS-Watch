# Simulator

The simulator is the first user-visible milestone. It should model a 466 × 466 circular display, touch, crown rotation/click, time, battery, BLE state, and sample notifications.

Prefer reusing LVGL UI code where the toolchain supports a simulator target. If the community Watch Pro 2 emulator is used, document its exact version and separate its functionality from this repository's own code.

No simulator interaction should trigger a real firmware update.
