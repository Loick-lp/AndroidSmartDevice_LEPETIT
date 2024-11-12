package fr.isen.lepetit.androidsmartservice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.isen.lepetit.androidsmartservice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSmartDeviceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScanScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ScanScreen(modifier: Modifier = Modifier) {
    // Variables pour gérer l'état de l'interface
    var isScanning by remember { mutableStateOf(false) }
    val devices = remember { mutableStateListOf<String>() }

    Column(
        modifier = modifier
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

        // Affichage de la barre de chargement circulaire si le scan est en cours
        if (isScanning) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        }

        // Bouton pour démarrer/arrêter le scan
        Button(
            onClick = {
                isScanning = !isScanning
            }
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

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    AndroidSmartDeviceTheme {
        ScanScreen()
    }
}
