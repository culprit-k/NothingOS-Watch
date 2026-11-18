# Nothing Music phone-to-watch protocol (NM1)

The watch app polls the app channel by sending `NM1R` (ASCII) on launch and every four seconds. A companion host should answer with a metadata snapshot and, when available, an album-art thumbnail.

All packets are at most 128 bytes. The app treats every packet as untrusted.

## Metadata

`NM1M | state:u8 | title_len:u8 | artist_len:u8 | title_utf8 | artist_utf8`

The three-byte prefix is `NM1`, followed by `M`. State: 0 = unavailable, 1 = paused, 2 = playing. Title and artist are UTF-8, each limited to 48 bytes. Packet length must equal 7 + title length + artist length.

## Artwork

- Begin: `NM1B | width:u8 | height:u8` (currently exactly 32×32).
- Chunk: `NM1C | offset:u16 little-endian | RGB565 little-endian bytes`. Chunks must be sequential and no larger than 122 image bytes.
- Clear: `NM1X`.
- 32×32 RGB565 is 2048 bytes and takes 17 maximum-size chunks.

The phone/host must resize and convert artwork before sending it. The watch app draws it only after all 2048 bytes have arrived. A broken or out-of-order transfer is rejected; send a new begin packet to restart.

## Host transport

Use the cmf-watch-sdk app channel. On a Mac, `wp2apps channel` / `wp2apps send` can be used for development. An iPhone companion must use the stub's channel key `K` on the existing Nothing X BLE connection (not open a second independent central connection). The upstream SDK documents the frame format and iOS shared-link design in `docs/abi-1.4.md`. No production iOS companion implementation is included in this commit.
