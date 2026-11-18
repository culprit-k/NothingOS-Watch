package org.nothingos.watchinstaller

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.zip.CRC32
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Wire-format primitives transcribed from the public fmctools/cmf-watch-sdk.
 * No pairing secret is stored, logged, or transmitted anywhere except the watch GATT session.
 */
internal object Wp2Protocol {
    const val SHELL_WRITE = "77d4ff01-2fe2-2334-0d35-9ccd078f529c"
    const val SHELL_NOTIFY = "77d4ff02-2fe2-2334-0d35-9ccd078f529c"
    const val COMMAND_WRITE = "0000fff2-0000-1000-8000-00805f9b34fb"
    const val COMMAND_NOTIFY = "0000fff1-0000-1000-8000-00805f9b34fb"
    const val DATA_NOTIFY = "02f00000-0000-0000-0000-00000000ffe2"
    const val SYSTEM = 0xFFFF
    const val INFO = 0x90A0
    const val BEGIN = 0x90A1
    const val DATA = 0x90A2
    const val END = 0x90A3
    const val DELETE = 0x90A4
    const val MAX_DATA = 128
    private val IV = hex("5051525354555657606162636465665A")

    data class Frame(val c1: Int, val c2: Int, val body: ByteArray)

    fun crc32(bytes: ByteArray): Long = CRC32().apply { update(bytes) }.value

    fun encodeFrame(c1: Int, c2: Int, payload: ByteArray, key: ByteArray?): ByteArray {
        val body = if (key == null) {
            payload + le32(crc32(payload))
        } else encrypt(key, payload)
        require(body.size <= 0xFFFF)
        val header = ByteBuffer.allocate(11).order(ByteOrder.BIG_ENDIAN)
            .put(0xF5.toByte()).putShort(body.size.toShort()).putShort(c1.toShort())
            .putShort(1).putShort(1).putShort(c2.toShort()).array()
        return header + body
    }

    fun decryptPayload(key: ByteArray, ciphertext: ByteArray): ByteArray {
        require(ciphertext.isNotEmpty() && ciphertext.size % 16 == 0) { "Invalid encrypted frame length" }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(IV))
        val decoded = cipher.doFinal(ciphertext)
        require(decoded.size >= 4) { "Missing payload CRC" }
        val payload = decoded.copyOfRange(0, decoded.size - 4)
        val expected = readLe32(decoded, decoded.size - 4)
        require(crc32(payload) == expected) { "Payload CRC mismatch" }
        return payload
    }

    fun encryptPayload(key: ByteArray, payload: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(IV))
        return cipher.doFinal(payload + le32(crc32(payload)))
    }

    fun parsePackage(data: ByteArray): PackageInfo {
        require(data.size >= 64) { "Package is shorter than its 64-byte header" }
        require(data.copyOfRange(0, 4).contentEquals("WP2A".toByteArray(Charsets.US_ASCII))) { "Not a WP2A package" }
        val b = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val abi = b.getShort(4).toInt() and 0xFFFF
        val kind = data[6].toInt() and 255
        val minor = data[7].toInt() and 255
        require(abi == 1 && kind in 0..1) { "Unsupported package ABI or kind" }
        require(data.copyOfRange(56, 64).all { it.toInt() == 0 }) { "Reserved package header bytes are not zero" }
        require(data.copyOfRange(8, 32).any { it.toInt() != 0 }) { "Package name is empty" }
        val nameBytes = data.copyOfRange(8, 32).takeWhile { it.toInt() != 0 }.toByteArray()
        val name = nameBytes.toString(Charsets.UTF_8)
        val version = b.getInt(32).toLong() and 0xFFFFFFFFL
        val imageSize = b.getInt(36).toLong() and 0xFFFFFFFFL
        val count = b.getInt(44).toLong() and 0xFFFFFFFFL
        val expectedLength = 64L + imageSize + count * 4L
        require(expectedLength == data.size.toLong()) { "Package length does not match header" }
        require(crc32(data.copyOfRange(64, data.size)) == (b.getInt(48).toLong() and 0xFFFFFFFFL)) { "Body CRC mismatch" }
        require(crc32(data.copyOfRange(0, 52)) == (b.getInt(52).toLong() and 0xFFFFFFFFL)) { "Header CRC mismatch" }
        return PackageInfo(name, version, kind, minor, data.size)
    }

    /** Parse the 64-byte package header returned by INFO for an installed slot. */
    fun parseInfoHeader(header: ByteArray): PackageInfo {
        require(header.size == 64) { "Installed package header must be exactly 64 bytes" }
        require(header.copyOfRange(0, 4).contentEquals("WP2A".toByteArray(Charsets.US_ASCII))) {
            "Installed slot does not contain a WP2A package"
        }
        val b = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        val abi = b.getShort(4).toInt() and 0xFFFF
        val kind = header[6].toInt() and 0xFF
        val minor = header[7].toInt() and 0xFF
        require(abi == 1 && kind in 0..1) { "Unsupported installed package ABI or kind" }
        require(header.copyOfRange(56, 64).all { it.toInt() == 0 }) { "Installed header reserved bytes are not zero" }
        require(crc32(header.copyOfRange(0, 52)) == (b.getInt(52).toLong() and 0xFFFFFFFFL)) {
            "Installed package header CRC mismatch"
        }
        val nameBytes = header.copyOfRange(8, 32).takeWhile { it.toInt() != 0 }.toByteArray()
        require(nameBytes.isNotEmpty()) { "Installed package name is empty" }
        val name = nameBytes.toString(Charsets.UTF_8)
        val version = b.getInt(32).toLong() and 0xFFFFFFFFL
        return PackageInfo(name, version, kind, minor, 64)
    }

    fun installBegin(slot: Int, pkg: ByteArray): ByteArray {
        require(slot in 0..255)
        val crc = crc32(pkg)
        return byteArrayOf(slot.toByte()) + le32(pkg.size.toLong()) + le32(crc)
    }

    fun installChunk(offset: Int, bytes: ByteArray): ByteArray {
        require(bytes.size <= MAX_DATA)
        return le32(offset.toLong()) + bytes
    }

    fun le32(value: Long): ByteArray = byteArrayOf(
        value.toByte(), (value shr 8).toByte(), (value shr 16).toByte(), (value shr 24).toByte()
    )

    fun readLe32(bytes: ByteArray, offset: Int): Long =
        (bytes[offset].toLong() and 255) or ((bytes[offset+1].toLong() and 255) shl 8) or
            ((bytes[offset+2].toLong() and 255) shl 16) or ((bytes[offset+3].toLong() and 255) shl 24)

    fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)

    fun hex(value: String): ByteArray {
        require(value.length % 2 == 0)
        return ByteArray(value.length / 2) { i -> value.substring(i*2, i*2+2).toInt(16).toByte() }
    }

    private fun encrypt(key: ByteArray, payload: ByteArray): ByteArray = encryptPayload(key, payload)

    data class PackageInfo(val name: String, val version: Long, val kind: Int, val abiMinor: Int, val byteSize: Int)

    class FrameParser {
        private var pending = ByteArray(0)
        fun feed(input: ByteArray): List<Frame> {
            pending += input
            val frames = mutableListOf<Frame>()
            while (true) {
                val start = pending.indexOf(0xF5.toByte())
                if (start < 0) { pending = ByteArray(0); break }
                if (start > 0) pending = pending.copyOfRange(start, pending.size)
                if (pending.size < 11) break
                val b = ByteBuffer.wrap(pending).order(ByteOrder.BIG_ENDIAN)
                val length = b.getShort(1).toInt() and 0xFFFF
                val c1 = b.getShort(3).toInt() and 0xFFFF
                val c2 = b.getShort(9).toInt() and 0xFFFF
                val bodyLength = if (c1 == SYSTEM && c2 == 0x0048) length - 4 else length
                if (bodyLength < 0) { pending = pending.copyOfRange(1, pending.size); continue }
                if (pending.size < 11 + bodyLength) break
                frames += Frame(c1, c2, pending.copyOfRange(11, 11 + bodyLength))
                pending = pending.copyOfRange(11 + bodyLength, pending.size)
            }
            return frames
        }
    }
}
