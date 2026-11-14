# YouTube feature — deferred, not implemented

## Original request and clarification

The requested experience was a native YouTube application on the watch, not a phone-remote screen. The earlier YouTube Remote mock was only a speculative browser concept and did not meet that request.

## Current product decision

YouTube is **not in the current NothingOS Wear implementation scope**. The project has moved effort to the Music app and core watch services. No YouTube app, remote controller, video decoder, streaming service, or YouTube playback integration has been implemented.

## Why this remains deferred

A native YouTube experience would require a supported network path, a compatible application/runtime environment, video codecs, sustained throughput, memory, audio output, and any service/API access permitted by YouTube. The public D398/Actions SDK evidence currently available does not establish these capabilities for this exact watch firmware and hardware.

Do not present a remote-control mock as a native YouTube application. Do not invent playback, account login, streaming, or network support.

## Reconsideration criteria

Only reopen this feature after all of the following are independently verified:
- a network transport and Internet access path usable by a D398 application;
- supported video decoding and display pipeline;
- supported audio output during video playback;
- enough memory, storage and power budget for an acceptable experience;
- an authorized and maintainable YouTube playback/API path;
- a complete D398 build and a tested recovery route.

This document does not authorize firmware flashing. The D398 stock firmware, board port, package and recovery release gates remain in force.
