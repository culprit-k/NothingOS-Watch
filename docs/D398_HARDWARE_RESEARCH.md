# D398 hardware research baseline

Research date: 2026-11-14

## Exact-model public evidence

A recently updated community project, [firmware-wp2-badapple](https://github.com/cmf-tech-community/firmware-wp2-badapple), explicitly targets the CMF Watch Pro 2 / JX402 rather than Watch 3 Pro. Its README describes a Zephyr application using the Actions ATS3089C SDK. The project includes a dedicated `board/wp2` profile, reconstructed display/touch/input/audio drivers, and host validation for some driver state machines.

This is the strongest exact-model public hardware reference found in this research pass. It is still community reverse engineering, not an official Nothing/CMF schematic or a verified dump from the owner's exact watch. Its own hardware page says the board description is reconstructed from the stock application and live register captures, not from the original devicetree or a PCB schematic.

## Candidate hardware profile

The public Pro 2 reference reports the following candidate values:

| Component | Candidate detail | Confidence / limitation |
|---|---|---|
| SoC / board | Actions ATS3089C; board string `jx402_01_3089c` | Strong agreement across Pro 2 firmware research; exact owner-unit revision unverified |
| Display | CO5300 / JX07003, 466×466, RGB565/BGR baseline | Stock-derived community driver; physical test on owner's unit not done |
| Touch | CST820 on I2C0, 7-bit address 0x15; SCL A24, SDA A25, power A5, reset A26, IRQ A27 | Reconstructed from stock code/register evidence; no electrical schematic |
| Crown | MT3511, I2C2 address 0x75, IRQ GPIOB.26 | Community stock-derived driver and init table; physical owner-unit test not done |
| Heart-rate sensor | VC9202 | The Pro 2 board overlay selects VC9202; actual live sensor sampling not validated on the owner's unit |
| Battery / charging | Internal PMU charger, BATV ADC and reconstructed NTC/charge profile | Reconstructed, not an electrical design file |
| Audio | DAC/PMUDAC path and amplifier control GPIOA.21 | Audio output is demonstrated in the project's video, but its hardware document still marks some output validation as incomplete |
| Memory | 8 MiB PSRAM | Explicit setting in the Pro 2 board overlay; memory map still needs matching against exact device/firmware |

The values above are not silently treated as verified owner-unit facts. They are stored in `firmware/targets/d398/target.yaml` as *candidates*, along with their validation flags.

## What this changes for NothingOS Wear

- D398 is no longer an entirely unknown hardware target. There is a public, model-specific reconstruction to use as the primary investigation reference.
- The earlier generic ATS3089C development-board profile remains a compile-only target. Its panel, touch, PMU and pin mapping must not be substituted for the Pro 2 profile.
- The board implementation should be split from app logic: panel, touch, crown, battery, heart-rate, audio and storage each need independent interfaces and tests.
- The candidate CO5300/CST820/MT3511/VC9202 stack can guide implementation, but must not enable flashing by itself.
- A stock image from an older public dump carries a different firmware version string; do not assume it is byte-identical to the owner's reported 1.0.0.73.

## What remains unknown / release-blocking

1. The owner's exact D398 hardware revision and confirmation that its live peripherals match the candidate profile.
2. A hash/provenance match between any public stock baseline and the exact 1.0.0.73 firmware on the user's watch.
3. D398-specific board port build, including the exact linker/memory/partition limits.
4. Current stock bootloader/update gates, image signatures and all partition IDs for this unit.
5. A verified stock rollback package and tested hardware recovery route.
6. Physical tests of display init, touch, crown, battery/charging, sensor sampling, BLE, audio, sleep and wake.

## Public references inspected

- [Watch Pro 2 Bad Apple project](https://github.com/cmf-tech-community/firmware-wp2-badapple)
- [Its hardware reconstruction](https://github.com/cmf-tech-community/firmware-wp2-badapple/blob/main/docs/hardware.md)
- [Its stock-derived profile](https://github.com/cmf-tech-community/firmware-wp2-badapple/blob/main/board/wp2/stock-profile.json)
- [Its Pro 2 board overlay](https://github.com/cmf-tech-community/firmware-wp2-badapple/blob/main/app/boards/ats3089c_dev_watch.conf)
- [Its validation notes](https://github.com/cmf-tech-community/firmware-wp2-badapple/blob/main/docs/validation.md)
- [Actions ATS3089C public SDK, pinned revision](https://github.com/lvgl/lv_port_actions_technology/tree/26f51e584940d5b141cd1d09d2629d69b595f579)

The reference project's source repository has no blanket license for the whole tree. NothingOS Wear records its public hardware findings and writes its own target code; it does not copy the reference project's custom driver code wholesale.
