package fr.isen.lepetit.androidsmartservice.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScanComposable(
    isScanning: Boolean,
    devices: SnapshotStateList<String>,
    onScanToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titre
        Text(
            text = "Scan des appareils BLE",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Message d'état de scan
        Text(
            text = if (isScanning) "Scan en cours..." else "Scan arrêté",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Barre de chargement si le scan est en cours
        if (isScanning) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        }

        // Bouton pour démarrer/arrêter le scan
        Button(
            onClick = { onScanToggle() }
        ) {
            Text(text = if (isScanning) "Arrêter le scan" else "Lancer le scan BLE")
        }

        // Liste des appareils détectés
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            items(devices) { device ->
                Text(
                    text = device,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
