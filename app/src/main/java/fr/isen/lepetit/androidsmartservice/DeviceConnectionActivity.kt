package fr.isen.lepetit.androidsmartservice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.isen.lepetit.androidsmartservice.ui.theme.AndroidSmartDeviceTheme

class DeviceConnectionActivity : ComponentActivity() {
    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceAddress = intent.getStringExtra("device_address")
        val deviceName = intent.getStringExtra("device_name") ?: "Appareil inconnu"

        // Initialise le Bluetooth Adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        setContent {
            AndroidSmartDeviceTheme {
                var connectionStatus by remember { mutableStateOf("Connexion en cours à $deviceName...") }

                // Interface de connexion
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = connectionStatus, style = MaterialTheme.typography.headlineSmall)
                }

                // Démarre la connexion au périphérique
                deviceAddress?.let {
                    connectToDevice(it) { status ->
                        connectionStatus = status
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(deviceAddress: String, onStatusChange: (String) -> Unit) {
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        bluetoothGatt = device.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> onStatusChange("Connecté à ${gatt.device.name}")
                    BluetoothGatt.STATE_DISCONNECTED -> onStatusChange("Déconnecté de ${gatt.device.name}")
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
    }
}
