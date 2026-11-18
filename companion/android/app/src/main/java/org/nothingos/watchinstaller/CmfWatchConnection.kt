package org.nothingos.watchinstaller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Minimal host for the CMF Watch Pro 2 app channel.
 *
 * The pairing secret is kept only in memory. This class never performs firmware OTA,
 * factory reset, unpairing, slot deletion, or an implicit replacement.
 */
internal class CmfWatchConnection(
    context: Context,
    private val device: BluetoothDevice
) : BluetoothGattCallback() {
    private val appContext = context.applicationContext
    private val callbackLock = Any()
    private var gatt: BluetoothGatt? = null
    private var connected = false
    private var connectionStatus = BluetoothGatt.GATT_FAILURE
    private var servicesStatus = BluetoothGatt.GATT_FAILURE
    private val connectedLatch = CountDownLatch(1)
    private val servicesLatch = CountDownLatch(1)
    private var operationLatch: CountDownLatch? = null
    @Volatile private var operationStatus = BluetoothGatt.GATT_FAILURE

    private val shellNotifications = LinkedBlockingQueue<ByteArray>()
    private val commandFrames = LinkedBlockingQueue<Wp2Protocol.Frame>()
    private val parser = Wp2Protocol.FrameParser()
    @Volatile private var channelKey: ByteArray? = null
    @Volatile private var closed = false
    private var maxAbiMinor: Int? = null

    data class WatchState(val stubVersion: Long, val maxAbiMinor: Int?)
    data class InstalledPackage(val slot: Int, val name: String, val version: Long, val kind: Int)
    class ExistingPackageException(val slot: Int, val packageName: String, val version: Long) :
        Exception("$packageName v$version already exists in slot ${slotLabel(slot)}")

    fun connectAndProbe(): WatchState {
        try {
            val connectedGatt = device.connectGatt(appContext, false, this, BluetoothDevice.TRANSPORT_LE)
                ?: error("Android could not start the BLE connection")
            synchronized(callbackLock) { if (gatt == null) gatt = connectedGatt }
            if (!connectedLatch.await(18, TimeUnit.SECONDS)) throw TimeoutException("Timed out connecting to the watch")
            if (!connected || connectionStatus != BluetoothGatt.GATT_SUCCESS) {
                throw IllegalStateException("BLE connection failed (status $connectionStatus)")
            }

            val activeGatt = requireNotNull(gatt)
            if (!activeGatt.discoverServices()) error("Could not start GATT service discovery")
            if (!servicesLatch.await(12, TimeUnit.SECONDS)) throw TimeoutException("Timed out discovering watch services")
            if (servicesStatus != BluetoothGatt.GATT_SUCCESS) {
                error("Watch GATT service discovery failed (status $servicesStatus)")
            }

            subscribe(Wp2Protocol.COMMAND_NOTIFY)
            subscribe(Wp2Protocol.SHELL_NOTIFY)
            // The watch's stock host marks the phone ready after this third subscription.
            subscribe(Wp2Protocol.DATA_NOTIFY)
            Thread.sleep(600)

            val secretText = readShellField("GETSECRET")
            val key = Wp2Protocol.hex(secretText)
            require(key.size == 16 && key.any { it.toInt() != 0 }) {
                "The watch returned an empty pairing secret. Keep it paired with its normal phone app and retry."
            }
            channelKey = key

            val reply = request(Wp2Protocol.INFO, byteArrayOf(0xFE.toByte()),
                Wp2Protocol.INFO or 0xA000, timeoutSeconds = 3)
            if (reply.isEmpty()) error("The watch returned an empty stub response")
            val status = reply[0].toInt() and 0xFF
            when (status) {
                0 -> {
                    require(reply.size == 6) { "Unexpected app-stub information length: ${reply.size}" }
                    val version = Wp2Protocol.readLe32(reply, 1)
                    val abi = reply[5].toInt() and 0xFF
                    maxAbiMinor = abi
                    return WatchState(version, abi)
                }
                1 -> {
                    // Older stub: the slot-version query is not implemented, but the app channel works.
                    maxAbiMinor = null
                    return WatchState(1, null)
                }
                else -> error("Watch rejected the compatibility check (status $status)")
            }
        } catch (e: Exception) {
            close()
            if (e is TimeoutException) {
                throw UnsupportedWatchFirmwareException(
                    "The watch did not answer the app-stub check. Stock firmware does not include the app installer stub; " +
                        "a compatible watch-side setup is required first. This app will not flash firmware."
                )
            }
            throw e
        }
    }

    fun installPackage(packageBytes: ByteArray, allowReplace: Boolean = false): InstalledPackage {
        check(!closed) { "The watch is disconnected" }
        val pkg = Wp2Protocol.parsePackage(packageBytes)
        val limit = maxAbiMinor
        if (limit != null && pkg.abiMinor > limit) {
            error("This package needs ABI minor ${pkg.abiMinor}; the watch stub supports ${limit}.")
        }

        val existing = if (pkg.kind == 1) {
            readSlot(0xFF)?.let { listOf(it) } ?: emptyList()
        } else {
            (0 until 8).mapNotNull { readSlot(it) }.filter { it.name == pkg.name }
        }

        val slot: Int
        if (existing.isNotEmpty()) {
            val found = existing.first()
            if (!allowReplace) throw ExistingPackageException(found.slot, found.name, found.version)
            slot = found.slot
        } else if (pkg.kind == 1) {
            slot = 0xFF
        } else {
            val used = (0 until 8).mapNotNull { readSlot(it)?.slot }.toSet()
            slot = (0 until 8).firstOrNull { it !in used }
                ?: error("All eight app slots are occupied. No app was removed or overwritten.")
        }

        call(Wp2Protocol.BEGIN, Wp2Protocol.installBegin(slot, packageBytes))
        var offset = 0
        while (offset < packageBytes.size) {
            val end = minOf(offset + Wp2Protocol.MAX_DATA, packageBytes.size)
            val chunk = packageBytes.copyOfRange(offset, end)
            val ack = call(Wp2Protocol.DATA, Wp2Protocol.installChunk(offset, chunk))
            require(ack.size >= 4) { "The watch returned a short DATA acknowledgement at offset $offset" }
            val next = Wp2Protocol.readLe32(ack, 0)
            require(next == end.toLong()) { "The watch expects byte $next, but this transfer sent byte $end" }
            offset = end
        }
        call(Wp2Protocol.END, byteArrayOf(0))

        val installed = readSlot(slot) ?: error("Transfer ended, but the watch did not report the installed package")
        require(installed.name == pkg.name && installed.version == pkg.version && installed.kind == pkg.kind) {
            "Post-install verification did not match the selected package; reported ${installed.name} v${installed.version}"
        }
        return installed
    }

    private fun readSlot(slot: Int): InstalledPackage? {
        val raw = call(Wp2Protocol.INFO, byteArrayOf(slot.toByte()))
        if (raw.isEmpty()) return null
        require(raw.size == 64) { "Slot $slot returned an unexpected header length: ${raw.size}" }
        val info = Wp2Protocol.parseInfoHeader(raw)
        return InstalledPackage(slot, info.name, info.version, info.kind)
    }

    private fun call(command: Int, payload: ByteArray): ByteArray {
        val reply = request(command, payload, command or 0xA000)
        require(reply.isNotEmpty()) { "The watch returned an empty reply to ${command.toString(16)}" }
        val status = reply[0].toInt() and 0xFF
        if (status != 0) {
            val label = when (status) {
                1 -> "BAD_REQUEST"; 2 -> "IO"; 3 -> "SEQUENCE"; 4 -> "CRC"
                5 -> "BUSY"; 6 -> "INVALID"; 7 -> "NO_APP"; 8 -> "FULL"; 9 -> "PAUSED"
                else -> "status $status"
            }
            error("The watch refused command 0x${command.toString(16)}: $label")
        }
        return reply.copyOfRange(1, reply.size)
    }

    private fun request(command: Int, payload: ByteArray, expected: Int, timeoutSeconds: Long = 6): ByteArray {
        val key = channelKey ?: error("The authenticated watch channel is not open")
        writeCharacteristic(Wp2Protocol.COMMAND_WRITE,
            Wp2Protocol.encodeFrame(Wp2Protocol.SYSTEM, command, payload, key), response = false)
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds)
        while (true) {
            val remaining = deadline - System.nanoTime()
            if (remaining <= 0) throw TimeoutException("Timed out waiting for watch command 0x${expected.toString(16)}")
            val frame = commandFrames.poll(remaining, TimeUnit.NANOSECONDS)
                ?: throw TimeoutException("Timed out waiting for watch command 0x${expected.toString(16)}")
            if (frame.c1 != Wp2Protocol.SYSTEM || frame.c2 != expected) continue
            try {
                return Wp2Protocol.decryptPayload(key, frame.body)
            } catch (_: Exception) {
                // Other clients (for example the iPhone companion) share notifications on this link.
            }
        }
    }

    private fun readShellField(field: String): String {
        writeCharacteristic(Wp2Protocol.SHELL_WRITE, "AT $field\r\n".toByteArray(Charsets.US_ASCII), response = true)
        val buffer = StringBuilder()
        val pattern = Regex("$field:([0-9A-Fa-f]{32}),OK")
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8)
        while (System.nanoTime() < deadline) {
            val left = deadline - System.nanoTime()
            val part = shellNotifications.poll(left, TimeUnit.NANOSECONDS)
                ?: throw TimeoutException("Timed out reading AT $field from the watch")
            buffer.append(part.toString(Charsets.US_ASCII))
            val match = pattern.find(buffer)
            if (match != null) return match.groupValues[1]
            if (buffer.contains("ERROR") || buffer.contains("FAIL")) {
                error("The watch rejected AT $field: $buffer")
            }
            if (buffer.length > 2048) buffer.delete(0, buffer.length - 512)
        }
        throw TimeoutException("Timed out reading AT $field from the watch")
    }

    private fun subscribe(uuidText: String) {
        val activeGatt = requireNotNull(gatt)
        val characteristic = findCharacteristic(uuidText)
        val cccd = characteristic.getDescriptor(CCCD_UUID)
            ?: error("Notification descriptor missing for $uuidText")
        if (!activeGatt.setCharacteristicNotification(characteristic, true)) {
            error("Could not enable notifications for $uuidText")
        }
        val value = when {
            characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 ->
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0 ->
                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            else -> error("Characteristic $uuidText does not support notifications")
        }
        awaitGattCallback {
            if (Build.VERSION.SDK_INT >= 33) {
                activeGatt.writeDescriptor(cccd, value) == BluetoothStatusCodes.SUCCESS
            } else {
                @Suppress("DEPRECATION")
                cccd.value = value
                @Suppress("DEPRECATION")
                activeGatt.writeDescriptor(cccd)
            }
        }
    }

    private fun writeCharacteristic(uuidText: String, bytes: ByteArray, response: Boolean) {
        val activeGatt = requireNotNull(gatt)
        val characteristic = findCharacteristic(uuidText)
        val useResponse = response || bytes.size > activeGatt.mtu - 3
        val writeType = if (useResponse) BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            else BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        val start = {
            if (Build.VERSION.SDK_INT >= 33) {
                activeGatt.writeCharacteristic(characteristic, bytes, writeType) == BluetoothStatusCodes.SUCCESS
            } else {
                @Suppress("DEPRECATION")
                characteristic.writeType = writeType
                @Suppress("DEPRECATION")
                characteristic.value = bytes
                @Suppress("DEPRECATION")
                activeGatt.writeCharacteristic(characteristic)
            }
        }
        if (useResponse) awaitGattCallback(start) else if (!start()) {
            error("Android rejected the BLE write to $uuidText")
        }
    }

    private fun findCharacteristic(uuidText: String): BluetoothGattCharacteristic {
        val wanted = UUID.fromString(uuidText)
        return requireNotNull(gatt).services.asSequence()
            .flatMap { it.characteristics.asSequence() }
            .firstOrNull { it.uuid == wanted }
            ?: error("The watch does not expose required BLE characteristic $uuidText")
    }

    private fun awaitGattCallback(start: () -> Boolean) {
        val latch = CountDownLatch(1)
        synchronized(callbackLock) {
            check(operationLatch == null) { "Another BLE operation is still pending" }
            operationStatus = BluetoothGatt.GATT_FAILURE
            operationLatch = latch
        }
        try {
            if (!start()) error("Android rejected a BLE GATT operation")
            if (!latch.await(10, TimeUnit.SECONDS)) throw TimeoutException("Timed out waiting for a BLE GATT operation")
            if (operationStatus != BluetoothGatt.GATT_SUCCESS) {
                error("BLE GATT operation failed (status $operationStatus)")
            }
        } finally {
            synchronized(callbackLock) { if (operationLatch === latch) operationLatch = null }
        }
    }

    private fun finishGattOperation(status: Int) {
        operationStatus = status
        synchronized(callbackLock) { operationLatch }?.countDown()
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        this.gatt = gatt
        connectionStatus = status
        connected = status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED
        if (connected || newState == BluetoothProfile.STATE_DISCONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
            connectedLatch.countDown()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        servicesStatus = status
        servicesLatch.countDown()
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        finishGattOperation(status)
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        finishGattOperation(status)
    }

    @Deprecated("Android 13 uses the value overload")
    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        @Suppress("DEPRECATION")
        routeNotification(characteristic.uuid, characteristic.value ?: return)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        routeNotification(characteristic.uuid, value)
    }

    private fun routeNotification(uuid: UUID, bytes: ByteArray) {
        when (uuid) {
            UUID.fromString(Wp2Protocol.SHELL_NOTIFY) -> shellNotifications.offer(bytes.copyOf())
            UUID.fromString(Wp2Protocol.COMMAND_NOTIFY) ->
                parser.feed(bytes).forEach { commandFrames.offer(it) }
            else -> Unit // DATA_NOTIFY is subscribed as the watch's host-ready signal.
        }
    }

    fun close() {
        if (closed) return
        closed = true
        channelKey?.fill(0)
        channelKey = null
        try { gatt?.disconnect() } catch (_: SecurityException) { }
        try { gatt?.close() } catch (_: SecurityException) { }
        gatt = null
    }

    companion object {
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private fun slotLabel(slot: Int) = if (slot == 0xFF) "L" else slot.toString()
    }
}

internal class UnsupportedWatchFirmwareException(message: String) : Exception(message)
