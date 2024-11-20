package fr.isen.lepetit.androidsmartservice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import fr.isen.lepetit.androidsmartservice.ui.theme.AndroidSmartDeviceTheme

// Classe pour représenter un appareil BLE avec nom, adresse et RSSI
data class BLEDevice(val name: String?, val address: String, val rssi: Int)

class ScanActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val handler = Handler(Looper.getMainLooper())
    private val devices: SnapshotStateList<BLEDevice> = mutableStateListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidSmartDeviceTheme {
                var isScanning by remember { mutableStateOf(false) }

                ScanComposable(
                    isScanning = isScanning,
                    devices = devices,
                    onScanToggle = {
                        if (isScanning) {
                            stopScan()
                        } else {
                            startScan()
                        }
                        isScanning = !isScanning
                    }
                )
            }
        }
    }

    private fun startScan() {
        if (bluetoothAdapter?.isEnabled == true && allPermissionsGranted()) {
            devices.clear() // Réinitialiser la liste des appareils
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)

            // Arrêter le scan après 10 secondes
            handler.postDelayed({ stopScan() }, 10_000)
        } else {
            requestPermissions()
        }
    }

    private fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            super.onScanResult(callbackType, result)
            val deviceAddress = result.device.address
            val deviceName = if (ActivityCompat.checkSelfPermission(
                    this@ScanActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            } else {

            }
            result.device.name ?: "Inconnu"
            val rssi = result.rssi

            // Ajouter ou mettre à jour l'appareil dans la liste
            val existingDeviceIndex = devices.indexOfFirst { it.address == deviceAddress }
            if (existingDeviceIndex != -1) {
                devices[existingDeviceIndex] = devices[existingDeviceIndex].copy(rssi = rssi)
            } else {
                devices.add(BLEDevice(name = deviceName.toString(), address = deviceAddress, rssi = rssi))
            }
        }

        override fun onBatchScanResults(results: MutableList<android.bluetooth.le.ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { result ->
                val deviceAddress = result.device.address
                val deviceName = if (ActivityCompat.checkSelfPermission(
                        this@ScanActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                } else {

                }
                result.device.name ?: "Inconnu"
                val rssi = result.rssi

                val existingDeviceIndex = devices.indexOfFirst { it.address == deviceAddress }
                if (existingDeviceIndex != -1) {
                    devices[existingDeviceIndex] =
                        devices[existingDeviceIndex].copy(rssi = rssi)
                } else {
                    devices.add(BLEDevice(name = deviceName.toString(), address = deviceAddress, rssi = rssi))
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Gérer les erreurs ici
        }
    }

    private fun allPermissionsGranted(): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.all { permission ->
            ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            getRequiredPermissions(),
            PERMISSION_REQUEST_CODE
        )
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

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }
}

// Composable pour afficher la liste des appareils et le bouton de scan
@Composable
fun ScanComposable(
    isScanning: Boolean,
    devices: List<BLEDevice>,
    onScanToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titre de la page
        Text(
            text = "AndroidSmartDevice",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Bouton de scan avec une icône
        Button(
            onClick = onScanToggle,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = if (isScanning) "Arrêter le Scan BLE" else "Lancer le Scan BLE")
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // Ligne séparatrice
        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Liste des appareils BLE détectés
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(devices) { device ->
                Text(
                    text = "${device.name ?: "Appareil inconnu"} (${device.address}), RSSI: ${device.rssi}",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}



@Preview
@Composable
fun PreviewScanComposable() {
    AndroidSmartDeviceTheme {
        ScanComposable(
            isScanning = false,
            devices = listOf(
                BLEDevice("Appareil A", "00:11:22:33:44:55", -50),
                BLEDevice("Appareil B", "66:77:88:99:AA:BB", -70)
            ),
            onScanToggle = {}
        )
    }
}

