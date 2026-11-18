# Nothing Music — CMF Watch Pro 2

A Rust application for the cmf-watch-sdk loader. This is a native watch app, not a firmware replacement.

## Implemented in this source

- Round-screen Now Playing interface.
- ABI 1.3 phone-media availability and playback-state events.
- Play, pause, previous and next via the SDK media-key API.
- A bounded host-message protocol for track metadata and 32×32 RGB565 album-cover thumbnails.
- Phone-control mode kept separate from standalone watch playback.

## Explicit limitations

- No audio decoder or watch-speaker output is implemented.
- No iOS companion currently transfers metadata/artwork.
- No assumption is made that iOS exposes Apple Music or Spotify global Now Playing artwork to third-party apps.
- Volume is not exposed by the documented SDK media-key API.
- This source has not yet been built or tested on a physical D398.

The physical speaker and the Actions SDK's A2DP/local-player demos do not prove that this Rust app can drive D398 audio hardware. That requires a separate D398-specific backend and physical output test.

## Build

Clone fmctools/cmf-watch-sdk next to this repository, or set CMF_WATCH_SDK to its checkout:

    CMF_WATCH_SDK=../cmf-watch-sdk ./scripts/build-fmctools-app.sh

See the upstream SDK docs/app-guide.md for toolchain, loader and installation prerequisites. This app requires ABI 1.3 media support.

## Host packet protocol

All messages are at most 128 bytes:

- Metadata: [0x01, state, title_len, artist_len, title_utf8, artist_utf8]. State: 0 unavailable, 1 paused, 2 playing. Title and artist max 48 bytes each.
- Artwork begin: [0x02, 32, 32].
- Artwork chunk: [0x03, offset_u16_le, RGB565 little-endian bytes]. Sequential chunks, up to 125 data bytes.
- Clear artwork: [0x04].

The host converts cover art to a 32×32 RGB565 thumbnail. It takes 2048 bytes and 17 maximum-size chunks. The app renders pixels as 4×4 rectangles.

## Controls

Three touch zones provide previous, play/pause and next. Crown rotation currently reports volume unavailable. Crown press exits through the loader's BUTTON event.

## Remaining integration gates

1. Build and unit-test against the pinned SDK.
2. Test media_key and MEDIA events on a physical D398 paired with the iPhone.
3. Implement an authenticated phone-side SDK host transport for metadata/artwork; wp2apps send is only a developer test route.
4. Verify whether the iOS/CMF media path provides AVRCP metadata and cover art.
5. Separately port the Actions local-music/A2DP stack to the exact D398 output route and prove audible output.
