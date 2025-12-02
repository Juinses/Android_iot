package com.example.practica.screen

import android.content.Context
import android.hardware.camera2.CameraManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.screen.home.SensorViewModel

@Composable
fun SensorsScreen(nav: NavController, vm: SensorViewModel = viewModel()) {
    val state by vm.uiState
    val context = LocalContext.current

    // Estados locales para Ampolleta y Linterna
    var isBulbOn by remember { mutableStateOf(false) }
    var isFlashlightOn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sensores IOT", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN 1: DATOS DE LA API (Temperatura y Humedad) ---
        if (state.isLoading && state.temperature == null) {
            CircularProgressIndicator()
            Text("Cargando sensores...", color = MaterialTheme.colorScheme.onBackground)
        } else {
            // Muestra error si existe, pero sigue mostrando datos antiguos si los hay
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            SensorDataCard(
                temperature = state.temperature,
                humidity = state.humidity,
                lastUpdate = state.lastUpdate
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECCIÓN 2: CONTROL DE DISPOSITIVOS (Ampolleta y Linterna) ---
        Text("Control de Dispositivos", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // AMPOLLETA (Simulada)
            DeviceControlCard(
                label = "Ampolleta",
                isOn = isBulbOn,
                icon = Icons.Default.Settings, // Usamos un ícono genérico
                onToggle = { isBulbOn = !isBulbOn }
            )

            // LINTERNA (Real)
            DeviceControlCard(
                label = "Linterna",
                isOn = isFlashlightOn,
                icon = Icons.Default.Notifications, // Usamos ícono genérico
                onToggle = {
                    val success = toggleFlashlight(context, !isFlashlightOn)
                    if (success) {
                        isFlashlightOn = !isFlashlightOn
                    }
                }
            )
        }
        
        // MENSAJES DE ESTADO (Requisito Punto 11)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isBulbOn) "Ampolleta encendida" else "Ampolleta apagada",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isBulbOn) MaterialTheme.colorScheme.primary else Color.Gray
        )
        Text(
            text = if (isFlashlightOn) "Linterna activada" else "Linterna desactivada",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isFlashlightOn) MaterialTheme.colorScheme.primary else Color.Gray
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun SensorDataCard(temperature: Float?, humidity: Float?, lastUpdate: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // TEMPERATURA
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Lógica: > 20 °C es "Alta", <= 20 °C es "Baja"
                val isHighTemp = (temperature ?: 0f) > 20f
                
                val tempColor = if (isHighTemp) Color.Red else Color.Blue
                // Icono dinámico: Warning (alerta/calor) si es alta, Info (frío/info) si es baja
                val tempIcon = if (isHighTemp) Icons.Default.Warning else Icons.Default.Info

                Icon(
                    imageVector = tempIcon,
                    contentDescription = null,
                    tint = tempColor,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Temperatura", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = temperature?.let { "$it °C" } ?: "-- °C",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // HUMEDAD
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite, // Ícono genérico simulando gota
                    contentDescription = null,
                    tint = Color.Cyan,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Humedad", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = humidity?.let { "$it %" } ?: "-- %",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (lastUpdate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Actualizado: $lastUpdate",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DeviceControlCard(
    label: String,
    isOn: Boolean,
    icon: ImageVector,
    onToggle: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isOn) Color(0xFFFFD700) else Color.LightGray) // Amarillo vs Gris
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = MaterialTheme.colorScheme.onBackground)
        Text(
            text = if (isOn) "ON" else "OFF",
            fontWeight = FontWeight.Bold,
            color = if (isOn) Color(0xFFDAA520) else Color.Gray
        )
    }
}

fun toggleFlashlight(context: Context, turnOn: Boolean): Boolean {
    return try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] // Generalmente la cámara trasera es la 0
        cameraManager.setTorchMode(cameraId, turnOn)
        true
    } catch (e: Exception) {
        Toast.makeText(context, "Error linterna: ${e.message}", Toast.LENGTH_SHORT).show()
        false
    }
}