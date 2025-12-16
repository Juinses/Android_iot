package com.example.practica.screen.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.data.remote.dto.AccessEventDto
import com.example.practica.ui.theme.ErrorRed
import com.example.practica.ui.theme.PracticaTheme
import com.example.practica.ui.theme.SuccessGreen

@Composable
fun HistoryScreen(
    nav: NavController,
    userId: Int? = null,
    departmentId: Int, // Agregado: Requerido por el backend
    vm: HistoryViewModel = viewModel()
) {
    val state by vm.uiState

    LaunchedEffect(Unit) {
        // Ahora pasamos el departmentId obligatorio
        vm.loadEvents(departmentId, userId)
    }

    HistoryScreenContent(
        state = state,
        userId = userId,
        onBack = { nav.popBackStack() },
        onRefresh = { vm.loadEvents(departmentId, userId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenContent(
    state: HistoryUiState,
    userId: Int?,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (userId == null) "Historial General" else "Mi Historial") 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Error al cargar datos", color = ErrorRed, fontWeight = FontWeight.Bold)
                    Text(text = state.error, fontSize = 12.sp, color = Color.Gray)
                }
            } else if (state.events.isEmpty()) {
                Text(
                    text = "No hay registros de acceso recientes.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.events) { event ->
                        HistoryItem(event)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(event: AccessEventDto) {
    // CORRECCIÓN: "ACCESO DENEGADO" o "DENEGADO" en el resultado deben mostrarse como no permitidos.
    // Lógica anterior podía ser laxa. Ahora:
    val isAllowed = event.result == "PERMITIDO" || 
                    (event.eventType.contains("VALIDO") && event.result != "DENEGADO")
    
    val statusColor = if (isAllowed) SuccessGreen else ErrorRed
    val icon = if (isAllowed) Icons.Default.Check else Icons.Default.Close

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAllowed) Color.Transparent else ErrorRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(statusColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Título: Tipo de Evento o Resultado directo si es denegado
                val title = if (!isAllowed) "Acceso Denegado" else event.eventType.replace("_", " ")
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAllowed) MaterialTheme.colorScheme.onSurface else ErrorRed
                )
                
                if (!event.userName.isNullOrEmpty()) {
                    Text(
                        text = event.userName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Mostrar detalle si existe (ej: "Sensor Bloqueado")
                if (!event.detail.isNullOrEmpty() && event.detail != "null") {
                     Text(
                        text = event.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
                    )
                }

                Text(
                    text = event.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = event.origin,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryPreview() {
    PracticaTheme {
        HistoryScreenContent(
            state = HistoryUiState(
                events = listOf(
                    AccessEventDto(1, "12:30", 1, "Juan Perez", "ACCESO VALIDO", "RFID", "PERMITIDO", null),
                    AccessEventDto(2, "12:35", 2, "Maria Lopez", "ACCESO DENEGADO", "RFID", "DENEGADO", "Sensor Bloqueado"),
                    AccessEventDto(3, "12:40", 1, "Admin", "APERTURA MANUAL", "APP", "PERMITIDO", null)
                )
            ),
            userId = null,
            onBack = {},
            onRefresh = {}
        )
    }
}
