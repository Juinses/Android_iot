package com.example.practica.screen.user_management

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.data.remote.dto.AccessSensorDto
import com.example.practica.data.remote.dto.UserDto
import com.example.practica.ui.theme.ErrorRed
import com.example.practica.ui.theme.PracticaTheme
import com.example.practica.ui.theme.SuccessGreen

@Composable
fun RfidManagementScreen(
    nav: NavController,
    vm: RfidViewModel = viewModel()
) {
    val state by vm.uiState
    val context = LocalContext.current
    
    // Feedback
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.clearMessages()
        }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearMessages()
        }
    }

    RfidManagementContent(
        state = state,
        onBack = { nav.popBackStack() },
        onCreateSensor = { code, type, userId -> 
            val userDept = state.users.find { it.id == userId }?.departmentId
            vm.createSensor(code, type, userId, userDept) 
        },
        onToggleStatus = { vm.toggleSensorStatus(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfidManagementContent(
    state: RfidUiState,
    onBack: () -> Unit,
    onCreateSensor: (String, String, Int) -> Unit,
    onToggleStatus: (AccessSensorDto) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión Tags RFID") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Sensor", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (state.isLoading && state.sensors.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.sensors) { sensor ->
                        val ownerName = state.users.find { it.id == sensor.userId }?.name ?: "ID: ${sensor.userId}"
                        SensorItem(
                            sensor = sensor,
                            ownerName = ownerName,
                            onToggleStatus = { onToggleStatus(sensor) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        AddSensorDialog(
            users = state.users,
            onDismiss = { showDialog = false },
            onConfirm = { code, type, userId ->
                onCreateSensor(code, type, userId)
                showDialog = false
            }
        )
    }
}

@Composable
fun SensorItem(
    sensor: AccessSensorDto,
    ownerName: String,
    onToggleStatus: () -> Unit
) {
    val isActive = sensor.status == "ACTIVO"
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) Color.Transparent else ErrorRed.copy(alpha=0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (sensor.type == "Tarjeta") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (sensor.type == "Tarjeta") Icons.Default.Settings else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.macAddress,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$ownerName (${sensor.type})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = sensor.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) SuccessGreen else ErrorRed,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Switch(
                checked = isActive,
                onCheckedChange = { onToggleStatus() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSensorDialog(
    users: List<UserDto>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Llavero") }
    var selectedUser by remember { mutableStateOf<UserDto?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Sensor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código MAC / UID") },
                    singleLine = true,
                    placeholder = { Text("Ej: A4:F3:11:00") }
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == "Llavero",
                        onClick = { selectedType = "Llavero" }
                    )
                    Text("Llavero")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = selectedType == "Tarjeta",
                        onClick = { selectedType = "Tarjeta" }
                    )
                    Text("Tarjeta")
                }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedUser?.name ?: "Seleccionar Dueño",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.name} ${user.lastName ?: ""}") },
                                onClick = {
                                    selectedUser = user
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && selectedUser != null) {
                        onConfirm(code, selectedType, selectedUser!!.id)
                    }
                },
                enabled = code.isNotBlank() && selectedUser != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RfidPreview() {
    PracticaTheme {
        RfidManagementContent(
            state = RfidUiState(
                sensors = listOf(
                    AccessSensorDto(1, "AA:BB:CC:11", "Tarjeta", 1, "ACTIVO"),
                    AccessSensorDto(2, "11:22:33:44", "Llavero", 2, "BLOQUEADO")
                ),
                users = listOf(
                    UserDto(1, "Juan", "Pérez", "juan@test.com"),
                    UserDto(2, "Maria", "Lopez", "maria@test.com")
                )
            ),
            onBack = {},
            onCreateSensor = { _, _, _ -> },
            onToggleStatus = {}
        )
    }
}
