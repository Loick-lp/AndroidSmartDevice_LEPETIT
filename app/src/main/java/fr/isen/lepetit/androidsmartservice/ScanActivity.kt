package fr.isen.lepetit.androidsmartservice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import fr.isen.lepetit.androidsmartservice.composable.ScanComposable
import fr.isen.lepetit.androidsmartservice.ui.theme.AndroidSmartDeviceTheme


// Classe pour représenter un appareil BLE
data class BLEDevice(val name: String?, val address: String, val rssi: Int)

class ScanActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val devices = mutableStateListOf<BLEDevice>() // Liste d'appareils BLE détectés
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialisation du launcher pour les permissions
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                startScan()
            } else {
                showPermissionDeniedMessage()
            }
        }

        setContent {
            AndroidSmartDeviceTheme {
                var isScanning by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf("") }

                ScanComposable(
                    isScanning = isScanning,
                    devices = devices,
                    errorMessage = errorMessage,
                    onScanToggle = {
                        when {
                            bluetoothAdapter == null -> {
                                errorMessage = "Bluetooth non disponible sur cet appareil."
                            }
                            bluetoothAdapter?.isEnabled == false -> {
                                errorMessage = "Bluetooth non activé. Veuillez l'activer."
                            }
                            else -> {
                                errorMessage = ""
                                if (isScanning) {
                                    stopScan()
                                } else {
                                    if (allPermissionsGranted()) {
                                        startScan()
                                    } else {
                                        requestPermissions()
                                    }
                                }
                                isScanning = !isScanning
                            }
                        }
                    },
                    onDeviceClick = { device -> navigateToDeviceConnection(device) } // Appel de la fonction de navigation
                )
            }
        }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(getRequiredPermissions())
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return getRequiredPermissions().all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showPermissionDeniedMessage() {
        println("Permissions refusées. Le scan ne peut pas démarrer.")
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        devices.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        handler.postDelayed({ stopScan() }, 10_000)
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val deviceAddress = result.device.address
            val deviceName = result.device.name ?: "Inconnu"
            val rssi = result.rssi

            if (deviceName == "Inconnu") {
                return
            }

            val existingDeviceIndex = devices.indexOfFirst { it.address == deviceAddress }
            if (existingDeviceIndex != -1) {
                devices[existingDeviceIndex] = devices[existingDeviceIndex].copy(rssi = rssi)
            } else {
                devices.add(BLEDevice(name = deviceName, address = deviceAddress, rssi = rssi))
            }
        }

        override fun onScanFailed(errorCode: Int) {
            println("Erreur de scan BLE : code $errorCode")
        }
    }

    private fun navigateToDeviceConnection(device: BLEDevice) {
        val intent = Intent(this, DeviceConnectionActivity::class.java).apply {
            putExtra("name", device.name)
            putExtra("address", device.address)
        }
        startActivity(intent)
    }
}