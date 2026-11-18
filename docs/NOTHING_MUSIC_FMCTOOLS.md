# Nothing Music — CMF Watch Pro 2

## App architecture

The installable app is a `no_std` Rust binary built through the community `fmctools/apps` workflow and the pinned `cmf-watch-sdk`. It draws a Now Playing screen on the 466×466 RGB565 framebuffer.

## Verified SDK integration

- `Api::media_bind(None, None)` subscribes to phone-media state events.
- `Api::media_key` sends play, pause, previous and next to the phone.
- `MEDIA` events report playing state in `x` and phone/media availability in `y`.
- `Api::send/recv` provide the 128-byte host message channel.
- The current SDK app interface does not expose an audio decoder, audio stream or speaker-output API.

The media-control implementation requires the loader build that contains the phone-media support (2026-11-16 or newer). The API source is present in the SDK pinned by the community apps Makefile.

## Album-art and metadata protocol

The watch-side parser and renderer support a small `NM1` protocol:
- `NM1R`: request a fresh metadata/artwork snapshot.
- `NM1M`: state, title length, artist length, UTF-8 title and artist.
- `NM1B`: begin a 32×32 RGB565 artwork transfer.
- `NM1C`: sequential RGB565 artwork chunks.
- `NM1X`: clear artwork.

The parser rejects malformed lengths, invalid UTF-8, out-of-order chunks and oversized transfers. Unit tests cover these cases. If no image is received, the watch displays a deliberately generic vinyl placeholder.

The app-side protocol is implemented. A production iOS host that reads Apple Music/Spotify artwork and sends `NM1` packets is not included yet. The upstream SDK describes a shared-link iOS host design, but the community companion's phone services are not yet a released implementation. Do not mistake the protocol parser for a working iPhone artwork bridge.

## Speaker playback

This app controls music on the paired phone; it does not play decoded audio through the watch speaker. The public app ABI does not expose the speaker/DAC route. Standalone speaker playback would require a separate firmware-level audio service and D398 hardware validation, not a normal store app. No firmware or loader image is modified by this project.

## Build and validation

`scripts/build-fmctools-app.sh` overlays the crate into a clean checkout of `fmctools/apps`, then runs the same `make check`, `make test` and `make build` targets used by the community repository. GitHub Actions is configured to upload the resulting `.wp2app` package as a workflow artifact.

No physical D398 install or speaker test has been performed. The package must pass CI before it is treated as build-verified.
