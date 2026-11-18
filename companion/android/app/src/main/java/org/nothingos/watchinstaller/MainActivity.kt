package org.nothingos.watchinstaller

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var scanButton: Button
    private lateinit var connectButton: Button
    private lateinit var installButton: Button
    private var bluetooth: BluetoothAdapter? = null
    private var selectedDevice: BluetoothDevice? = null
    @Volatile private var watchConnection: CmfWatchConnection? = null
    private var selectedPackage: ByteArray? = null
    private var scanning = false
    private val worker = Executors.newSingleThreadExecutor()

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) scanWatch()
        else setStatus("Bluetooth permissions are required to find and connect to the watch.")
    }

    private val packagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Could not read the selected file")
            val info = Wp2Protocol.parsePackage(bytes)
            selectedPackage = bytes
            installButton.isEnabled = watchConnection != null
            setStatus(
                "Package verified: ${info.name} v${info.version}\n" +
                    "${bytes.size} bytes · ABI 1.${info.abiMinor}\n" +
                    if (watchConnection == null) "Now find and connect to your watch."
                    else "Ready for the watch-side compatibility check and installation."
            )
        } catch (e: Exception) {
            selectedPackage = null
            installButton.isEnabled = false
            setStatus("Package rejected: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetooth = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 36, 24, 24)
        }
        root.addView(TextView(this).apply {
            text = "NOTHING WATCH\nINSTALLER"
            textSize = 25f
            gravity = Gravity.CENTER
        })
        val scroll = ScrollView(this)
        status = TextView(this).apply {
            text = "Android phone-side app installer\n\n" +
                "1. Select a trusted .wp2app package\n" +
                "2. Find and connect to CMF Watch Pro 2\n" +
                "3. Check for the compatible watch-side app stub\n" +
                "4. Transfer and verify the package\n\n" +
                "Important: this app does not flash firmware. Stock watches without the required app stub cannot install apps."
            textSize = 16f
            setPadding(0, 24, 0, 24)
        }
        scroll.addView(status)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        root.addView(Button(this).apply {
            text = "SELECT .WP2APP"
            setOnClickListener { packagePicker.launch(arrayOf("application/octet-stream", "*/*")) }
        }, matchWidth())
        scanButton = Button(this).apply {
            text = "SCAN FOR CMF WATCH PRO 2"
            setOnClickListener { requestScanPermission() }
        }
        root.addView(scanButton, matchWidth())
        connectButton = Button(this).apply {
            text = "CONNECT AND CHECK WATCH"
            isEnabled = false
            setOnClickListener { connectWatch() }
        }
        root.addView(connectButton, matchWidth())
        installButton = Button(this).apply {
            text = "INSTALL VERIFIED PACKAGE"
            isEnabled = false
            setOnClickListener { installSelectedPackage() }
        }
        root.addView(installButton, matchWidth())
        root.addView(Button(this).apply {
            text = "DISCONNECT"
            setOnClickListener { disconnectWatch() }
        }, matchWidth())
        setContentView(root)
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun requestScanPermission() {
        val needed = if (android.os.Build.VERSION.SDK_INT >= 31) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        val missing = needed.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionRequest.launch(missing.toTypedArray()) else scanWatch()
    }

    private fun scanWatch() {
        val adapter = bluetooth
        if (adapter == null) {
            setStatus("This phone does not expose a Bluetooth adapter.")
            return
        }
        if (!adapter.isEnabled) {
            setStatus("Turn on Bluetooth, then scan again.")
            return
        }
        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            setStatus("Bluetooth LE scanning is unavailable on this phone.")
            return
        }
        try {
            selectedDevice = null
            connectButton.isEnabled = false
            scanning = true
            scanButton.isEnabled = false
            setStatus("Scanning… Keep the watch nearby and awake.")
            scanner.startScan(callback)
        } catch (e: SecurityException) {
            scanning = false
            scanButton.isEnabled = true
            setStatus("Bluetooth permission error: ${e.message}")
        } catch (e: Exception) {
            scanning = false
            scanButton.isEnabled = true
            setStatus("Could not start scanning: ${e.message}")
        }
    }

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val name = result.scanRecord?.deviceName ?: try {
                result.device.name
            } catch (_: SecurityException) { null } ?: return
            if (name.contains("CMF Watch Pro 2", ignoreCase = true)) {
                runOnUiThread {
                    selectedDevice = result.device
                    stopScan()
                    connectButton.isEnabled = true
                    setStatus(
                        "Watch discovered: $name\nSignal: ${result.rssi} dBm\n\n" +
                            "Tap CONNECT AND CHECK WATCH. This only checks compatibility; it does not flash firmware."
                    )
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            runOnUiThread {
                stopScan()
                setStatus("Bluetooth scan failed (code $errorCode).")
            }
        }
    }

    private fun connectWatch() {
        val device = selectedDevice ?: run {
            setStatus("Scan and select your CMF Watch Pro 2 first.")
            return
        }
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
            android.os.Build.VERSION.SDK_INT >= 31) {
            requestScanPermission()
            return
        }
        scanButton.isEnabled = false
        connectButton.isEnabled = false
        installButton.isEnabled = false
        setStatus("Connecting to the watch…\nOpening the authenticated app channel and checking its app stub.")
        worker.execute {
            val candidate = CmfWatchConnection(this, device)
            try {
                val state = candidate.connectAndProbe()
                watchConnection?.close()
                watchConnection = candidate
                runOnUiThread {
                    scanButton.isEnabled = true
                    connectButton.isEnabled = true
                    installButton.isEnabled = selectedPackage != null
                    setStatus(
                        "Compatible app stub detected.\nStub version: ${state.stubVersion}\n" +
                            "Maximum ABI minor: ${state.maxAbiMinor?.toString() ?: "legacy"}\n\n" +
                            if (selectedPackage == null) "Select a .wp2app package to continue."
                            else "Package is ready. Tap INSTALL VERIFIED PACKAGE."
                    )
                }
            } catch (e: Exception) {
                candidate.close()
                runOnUiThread {
                    scanButton.isEnabled = true
                    connectButton.isEnabled = selectedDevice != null
                    installButton.isEnabled = false
                    setStatus("Watch check stopped safely:\n${e.message}")
                }
            }
        }
    }

    private fun installSelectedPackage() {
        val bytes = selectedPackage ?: return
        val link = watchConnection ?: run {
            setStatus("Connect to a compatible watch before installing.")
            return
        }
        installButton.isEnabled = false
        setStatus("Installing package…\nKeep the watch and phone close. Do not disconnect during transfer.")
        worker.execute {
            try {
                val installed = link.installPackage(bytes, allowReplace = false)
                runOnUiThread {
                    installButton.isEnabled = selectedPackage != null && watchConnection != null
                    setStatus(
                        "Package transferred and verified.\nName: ${installed.name}\n" +
                            "Version: ${installed.version}\nSlot: ${if (installed.slot == 0xFF) "L (loader)" else installed.slot}\n\n" +
                            "This confirms package storage, not that the app launches successfully on physical hardware."
                    )
                }
            } catch (e: CmfWatchConnection.ExistingPackageException) {
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("Replace existing app?")
                        .setMessage(
                            "${e.packageName} v${e.version} already exists in slot " +
                                "${if (e.slot == 0xFF) "L" else e.slot}. Replace that same slot with the selected package?"
                        )
                        .setNegativeButton("Cancel") { _, _ ->
                            installButton.isEnabled = true
                            setStatus("No changes made. Existing app was kept.")
                        }
                        .setPositiveButton("Replace this app") { _, _ -> installWithReplacement(link, bytes) }
                        .show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    installButton.isEnabled = true
                    setStatus("Installation stopped: ${e.message}\nNo app slot was deliberately deleted.")
                }
            }
        }
    }

    private fun installWithReplacement(link: CmfWatchConnection, bytes: ByteArray) {
        installButton.isEnabled = false
        setStatus("Replacing the selected app in its existing slot… Keep the connection active.")
        worker.execute {
            try {
                val installed = link.installPackage(bytes, allowReplace = true)
                runOnUiThread {
                    installButton.isEnabled = true
                    setStatus(
                        "Replacement transferred and verified.\n${installed.name} v${installed.version} · " +
                            "slot ${if (installed.slot == 0xFF) "L" else installed.slot}\n" +
                            "Physical app launch has not been tested."
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    installButton.isEnabled = true
                    setStatus("Replacement stopped: ${e.message}")
                }
            }
        }
    }

    private fun disconnectWatch() {
        worker.execute {
            watchConnection?.close()
            watchConnection = null
            runOnUiThread {
                installButton.isEnabled = false
                connectButton.isEnabled = selectedDevice != null
                scanButton.isEnabled = true
                setStatus("Disconnected. No firmware or app slots were changed by disconnecting.")
            }
        }
    }

    private fun stopScan() {
        if (scanning) {
            try { bluetooth?.bluetoothLeScanner?.stopScan(callback) } catch (_: Exception) { }
        }
        scanning = false
        if (::scanButton.isInitialized) scanButton.isEnabled = true
    }

    private fun setStatus(message: String) {
        if (::status.isInitialized) status.text = message
    }

    override fun onDestroy() {
        stopScan()
        watchConnection?.close()
        watchConnection = null
        worker.shutdownNow()
        super.onDestroy()
    }
}
