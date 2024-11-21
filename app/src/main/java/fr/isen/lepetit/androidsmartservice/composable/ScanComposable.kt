package fr.isen.lepetit.androidsmartservice.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.isen.lepetit.androidsmartservice.BLEDevice
import fr.isen.lepetit.androidsmartservice.R

// Composable pour afficher la liste des appareils et le bouton de scan
@Composable
fun ScanComposable(
    isScanning: Boolean,
    devices: List<BLEDevice>,
    errorMessage: String,
    onScanToggle: () -> Unit,
    onDeviceClick: (BLEDevice) -> Unit // Ajout de onDeviceClick pour le clic sur un appareil
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Scanner BLE",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onScanToggle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isScanning) "Arrêter le scan" else "Démarrer le scan")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Barre de chargement si le scan est en cours
        if (isScanning) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            )
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(devices) { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDeviceClick(device) } // Appelle onDeviceClick au clic
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.droite),
                        contentDescription = "Appareil BLE",
                        tint = Color.Blue,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name ?: "Appareil inconnu",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Adresse : ${device.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "${device.rssi} dBm",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Divider()
            }
        }
    }
}