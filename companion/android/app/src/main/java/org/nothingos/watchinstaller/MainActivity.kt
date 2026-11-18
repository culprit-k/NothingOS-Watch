package org.nothingos.watchinstaller

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import java.util.zip.CRC32

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var scanButton: Button
    private lateinit var bluetooth: BluetoothAdapter
    private var selectedPackage: ByteArray? = null
    private var scanning = false

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.all { it }) scanWatch() else status.text = "Bluetooth permissions are required to find the watch."
    }
    private val packagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Could not read selected file")
            val info = Wp2Package.validate(bytes)
            selectedPackage = bytes
            status.text = "Package verified: ${info.name} v${info.version}\n${bytes.size} bytes · ABI ${info.abi}.
Watch transfer is not enabled in this build."
        } catch (e: Exception) {
            selectedPackage = null
            status.text = "Package rejected: ${e.message}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetooth = manager.adapter
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 48, 28, 28)
        }
        root.addView(TextView(this).apply { text = "NOTHING WATCH\nINSTALLER"; textSize = 25f; gravity = Gravity.CENTER })
        status = TextView(this).apply {
            text = "Phone-side companion · prototype 0.1.0\n\n1. Select a .wp2app package\n2. Discover your CMF Watch Pro 2\n3. Authenticated installation is the next milestone."
            textSize = 16f; setPadding(0, 32, 0, 24)
        }
        root.addView(status, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Button(this).apply { text = "SELECT .WP2APP"; setOnClickListener { packagePicker.launch(arrayOf("application/octet-stream", "*/*")) } })
        scanButton = Button(this).apply { text = "SCAN FOR CMF WATCH PRO 2"; setOnClickListener { requestScanPermission() } }
        root.addView(scanButton)
        root.addView(Button(this).apply { text = "STOP SCAN"; setOnClickListener { stopScan() } })
        setContentView(root)
    }

    private fun requestScanPermission() {
        val needed = if (android.os.Build.VERSION.SDK_INT >= 31)
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val missing = needed.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionRequest.launch(missing.toTypedArray()) else scanWatch()
    }

    private fun scanWatch() {
        if (!bluetooth.isEnabled) { status.text = "Turn on Bluetooth, then scan again."; return }
        try {
            scanning = true
            scanButton.isEnabled = false
            status.text = "Scanning for CMF Watch Pro 2…\nKeep the watch nearby and awake."
            bluetooth.bluetoothLeScanner.startScan(callback)
        } catch (e: SecurityException) { status.text = "Bluetooth permission error: ${e.message}" }
    }

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name = result.scanRecord?.deviceName ?: result.device.name ?: return
            if (name.contains("CMF Watch Pro 2", ignoreCase = true)) {
                runOnUiThread {
                    status.text = "Watch discovered: $name\nSignal: ${result.rssi} dBm\n\nDiscovery only. No connection or installation is attempted in this prototype."
                    stopScan()
                }
            }
        }
        override fun onScanFailed(errorCode: Int) {
            runOnUiThread { status.text = "BLE scan failed (code $errorCode)." ; stopScan() }
        }
    }

    private fun stopScan() {
        if (scanning) try { bluetooth.bluetoothLeScanner.stopScan(callback) } catch (_: Exception) {}
        scanning = false
        if (::scanButton.isInitialized) scanButton.isEnabled = true
    }

    override fun onDestroy() { stopScan(); super.onDestroy() }
}

private object Wp2Package {
    data class Info(val name: String, val version: Long, val abi: Int)
    fun validate(b: ByteArray): Info {
        require(b.size >= 64) { "File is shorter than the WP2A header" }
        require(String(b, 0, 4, Charsets.US_ASCII) == "WP2A") { "Missing WP2A magic" }
        fun u16(o: Int) = (b[o].toInt() and 255) or ((b[o+1].toInt() and 255) shl 8)
        fun u32(o: Int): Long = (b[o].toLong() and 255) or ((b[o+1].toLong() and 255) shl 8) or ((b[o+2].toLong() and 255) shl 16) or ((b[o+3].toLong() and 255) shl 24)
        require(u16(4) == 1) { "Unsupported package ABI" }
        val kind = b[6].toInt() and 255
        require(kind == 0 || kind == 1) { "Unknown package kind" }
        val nameBytes = b.copyOfRange(8, 32).takeWhile { it.toInt() != 0 }.toByteArray()
        val name = nameBytes.toString(Charsets.UTF_8)
        require(name.isNotBlank()) { "Package name is empty" }
        val version = u32(32)
        val imageSize = u32(36)
        val relocationCount = u32(44)
        require(imageSize <= Int.MAX_VALUE && relocationCount <= Int.MAX_VALUE) { "Package is too large" }
        val expected = 64L + imageSize + relocationCount * 4L
        require(expected == b.size.toLong()) { "Package length does not match header" }
        val crc = CRC32().apply { update(b, 64, b.size - 64) }.value
        require(crc == u32(48)) { "Image/relocation CRC mismatch" }
        val headerCrc = CRC32().apply { update(b, 0, 52) }.value
        require(headerCrc == u32(52)) { "Header CRC mismatch" }
        return Info(name, version, u16(4))
    }
}
