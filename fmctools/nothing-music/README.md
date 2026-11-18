# Nothing Music — CMF Watch Pro 2

A native Rust app for the CMF Watch Pro 2 JX402 using the community app SDK.

## Implemented

- Round-screen Now Playing UI.
- Native phone-media availability and playback-state events through `Api::media_bind`.
- Play, pause, previous and next through `Api::media_key`.
- Host-message receiver for track metadata and album-art thumbnails.
- A clear vinyl fallback when no album-art packet has arrived.
- Bounded parsing and unit tests for every host packet.

## Honest limitations

- The app does not decode audio or send sound to the watch speaker. The public app ABI has no audio-output or speaker API.
- The SDK's native music interface controls phone playback; it does not provide a local audio player.
- Track metadata and album art require a companion host to send the documented `NM1` messages. The app-side protocol is implemented, but a released iOS bridge for Apple Music/Spotify is not included yet.
- The app uses 32×32 RGB565 art (2048 bytes); this is a low-resolution preview, not full cover art.
- Physical D398 testing and app package build are pending CI.

## Build and test

The build helper clones the community app repository, overlays this app into its `apps/` workspace, then runs the same pinned SDK build and test targets used by the store:

```sh
bash scripts/build-fmctools-app.sh
```

To run the app on a physical watch, use the community `wp2apps install` command only after confirming that the patched firmware and loader are installed. No firmware is flashed by this app build.
