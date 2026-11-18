Nothing Music is a native CMF Watch Pro 2 app for controlling music playing on a paired phone. It uses the watch loader's phone-media interface for play, pause, previous, next and playback state. The app also defines a bounded message protocol for an optional companion host to send track metadata and a 32×32 RGB565 album-art thumbnail.

Album artwork is shown only when a companion host sends it; otherwise the app shows a vinyl placeholder. This repository does not yet include a released iOS companion service, so album-art synchronization from Apple Music or Spotify is not yet available out of the box.

## Requirements

The CMF Watch Pro 2 JX402 patched firmware and App Loader from the FMC firmware shelf. Phone media controls require a loader build from 2026-11-16 or newer. A phone must be paired and have active media for playback commands to work.
