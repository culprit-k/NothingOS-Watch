package org.nothingos.watchinstaller

import org.junit.Assert.*
import org.junit.Test

class Wp2ProtocolTest {
    @Test fun encryptedPayloadRoundTripsAndChecksCrc() {
        val key = ByteArray(16) { it.toByte() }
        val payload = "authenticated-install-protocol".toByteArray()
        val encrypted = Wp2Protocol.encryptPayload(key, payload)
        assertArrayEquals(payload, Wp2Protocol.decryptPayload(key, encrypted))
        encrypted[0] = (encrypted[0].toInt() xor 1).toByte()
        try {
            Wp2Protocol.decryptPayload(key, encrypted)
            fail("Corrupted ciphertext must not validate")
        } catch (_: Exception) { }
    }

    @Test fun frameParserBuffersSplitNotifications() {
        val payload = byteArrayOf(1, 2, 3, 4)
        val frame = Wp2Protocol.encodeFrame(Wp2Protocol.SYSTEM, 0x90A1, payload, ByteArray(16))
        val parser = Wp2Protocol.FrameParser()
        assertTrue(parser.feed(frame.copyOfRange(0, 5)).isEmpty())
        val parsed = parser.feed(frame.copyOfRange(5, frame.size))
        assertEquals(1, parsed.size)
        assertEquals(Wp2Protocol.SYSTEM, parsed[0].c1)
        assertEquals(0x90A1, parsed[0].c2)
        assertArrayEquals(payload, Wp2Protocol.decryptPayload(ByteArray(16), parsed[0].body))
    }

    @Test fun beginAndChunkPayloadsUseLittleEndianOffsets() {
        val packageBytes = byteArrayOf(9, 8, 7)
        val begin = Wp2Protocol.installBegin(2, packageBytes)
        assertEquals(2, begin[0].toInt())
        assertEquals(3L, Wp2Protocol.readLe32(begin, 1))
        val chunk = Wp2Protocol.installChunk(128, byteArrayOf(5, 6))
        assertEquals(128L, Wp2Protocol.readLe32(chunk, 0))
        assertArrayEquals(byteArrayOf(5, 6), chunk.copyOfRange(4, 6))
    }
    @Test fun installedSlotHeaderIsParsedAndValidated() {
        val bytes = ByteArray(64)
        val buffer = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        buffer.put("WP2A".toByteArray(Charsets.US_ASCII))
        buffer.putShort(1)
        buffer.put(0)
        buffer.put(2)
        val name = "vitalis".toByteArray(Charsets.UTF_8)
        buffer.put(name)
        buffer.position(32)
        buffer.putInt(7)
        buffer.putInt(100)
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.put(ByteArray(8))
        val crc = java.util.zip.CRC32().apply { update(bytes, 0, 52) }.value
        buffer.putInt(52, crc.toInt())

        val info = Wp2Protocol.parseInfoHeader(bytes)
        assertEquals("vitalis", info.name)
        assertEquals(7L, info.version)
        assertEquals(0, info.kind)
        assertEquals(2, info.abiMinor)

        bytes[56] = 1
        try {
            Wp2Protocol.parseInfoHeader(bytes)
            fail("Non-zero reserved bytes must be rejected")
        } catch (_: IllegalArgumentException) { }
    }

}
