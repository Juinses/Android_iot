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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.practica.ui.theme.ErrorRed
import com.example.practica.ui.theme.InfoBlue
import com.example.practica.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(nav: NavController, vm: SensorViewModel = viewModel()) {
    val state by vm.uiState
    val context = LocalContext.current

    // Estado local para Linterna
    var isFlashlightOn by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitor Ambiental") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- SECCIÓN 1: DATOS DE LA API (Temperatura y Humedad) ---
            if (state.isLoading && state.temperature == null) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (state.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = state.errorMessage ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                Text("Lecturas en Tiempo Real", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                
                SensorDataCard(
                    temperature = state.temperature,
                    humidity = state.humidity,
                    lastUpdate = state.lastUpdate
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECCIÓN 2: UTILIDADES ---
            Text("Utilidades del Dispositivo", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            // LINTERNA (Real) - Única utilidad física del móvil relevante aquí
            DeviceControlCard(
                label = "Linterna Móvil",
                isOn = isFlashlightOn,
                icon = Icons.Default.Notifications, // Ojo: Buscar icono Flash si es posible, sino Notifications sirve
                onToggle = {
                    val success = toggleFlashlight(context, !isFlashlightOn)
                    if (success) {
                        isFlashlightOn = !isFlashlightOn
                    }
                }
            )
        }
    }
}

@Composable
fun SensorDataCard(temperature: Float?, humidity: Float?, lastUpdate: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // TEMPERATURA
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isHighTemp = (temperature ?: 0f) > 28f // Umbral ajustado
                val tempColor = if (isHighTemp) WarningOrange else InfoBlue
                val tempIcon = if (isHighTemp) Icons.Default.Warning else Icons.Default.Info

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(tempColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tempIcon,
                        contentDescription = null,
                        tint = tempColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text("Temperatura", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(
                        text = temperature?.let { "$it °C" } ?: "-- °C",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            androidx.compose.material3.Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(24.dp))

            // HUMEDAD
            Row(verticalAlignment = Alignment.CenterVertically) {
                 Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Cyan.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite, // Gota (simulada)
                        contentDescription = null,
                        tint = Color(0xFF00ACC1),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text("Humedad Relativa", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(
                        text = humidity?.let { "$it %" } ?: "-- %",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (lastUpdate != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Última actualización: $lastUpdate",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    modifier = Modifier.align(Alignment.End)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isOn) Color(0xFFFFD700) else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            androidx.compose.material3.Switch(checked = isOn, onCheckedChange = { onToggle() })
        }
    }
}

fun toggleFlashlight(context: Context, turnOn: Boolean): Boolean {
    return try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] 
        cameraManager.setTorchMode(cameraId, turnOn)
        true
    } catch (e: Exception) {
        Toast.makeText(context, "Error linterna: ${e.message}", Toast.LENGTH_SHORT).show()
        false
    }
}
