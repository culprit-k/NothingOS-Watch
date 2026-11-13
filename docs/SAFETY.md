# Firmware safety and recovery policy

The CMF Watch Pro 2 has no conventional USB development port. Community research reports that a corrupted image may require opening the case and using internal SWD pads with a hardware programmer. Verify this for the exact board revision.

## Prohibited during early development
- No experimental flashing to the owner's only watch.
- No cross-flashing another CMF/Nothing model.
- No bypassing secure boot, signatures, rollback protection, or authentication.
- No unverified offsets, partitions, GPIO mappings, or board names.
- No interruption of official updates.
- No public stock firmware dumps unless redistribution rights are clear.

## Required before any device write
1. Exact model, board revision, and stock firmware version confirmed.
2. Private, unmodified stock backup and SHA-256 hash.
3. Verified image/container structure and board identifier.
4. Verified boot validation/signature policy and version constraints.
5. Tested recovery on a donor device of the same hardware revision.
6. Explicit understanding of possible permanent device loss.

Until all gates are satisfied, use source analysis, simulator, supported development board, and read-only BLE diagnostics only.

Never commit Bluetooth MAC addresses, pairing secrets, tokens, phone identifiers, personal notification payloads, or health data.
