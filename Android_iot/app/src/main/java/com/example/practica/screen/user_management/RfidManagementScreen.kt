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
    vm: RfidViewModel = viewModel(),
    isReadOnly: Boolean = false,
    filterUserId: Int? = null
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

    // Filtrado local para vista de Operador
    val displayedSensors = if (filterUserId != null) {
        state.sensors.filter { it.userId == filterUserId }
    } else {
        state.sensors
    }

    RfidManagementContent(
        state = state.copy(sensors = displayedSensors),
        isReadOnly = isReadOnly,
        onBack = { nav.popBackStack() },
        onCreateSensor = { code, type, userId, status -> 
            val userDept = state.users.find { it.id == userId }?.departmentId
            vm.createSensor(code, type, userId, userDept, status) 
        },
        onStatusChange = { sensor, newStatus -> vm.updateSensorStatus(sensor, newStatus) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RfidManagementContent(
    state: RfidUiState,
    isReadOnly: Boolean,
    onBack: () -> Unit,
    onCreateSensor: (String, String, Int, String) -> Unit,
    onStatusChange: (AccessSensorDto, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isReadOnly) "Mis Sensores" else "Gestión Tags RFID") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (!isReadOnly) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Sensor", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .background(MaterialTheme.colorScheme.background)
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
                            isReadOnly = isReadOnly,
                            onStatusChange = { newStatus -> onStatusChange(sensor, newStatus) }
                        )
                    }
                    if (state.sensors.isEmpty()) {
                        item {
                            Text(
                                "No hay sensores registrados.",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
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
            onConfirm = { code, type, userId, status ->
                onCreateSensor(code, type, userId, status)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorItem(
    sensor: AccessSensorDto,
    ownerName: String,
    isReadOnly: Boolean,
    onStatusChange: (String) -> Unit
) {
    val isActive = sensor.status == "ACTIVO"
    var expanded by remember { mutableStateOf(false) }
    
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
                    tint = if (sensor.type == "Tarjeta") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.macAddress,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$ownerName (${sensor.type})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sensor.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = when(sensor.status) {
                        "ACTIVO" -> SuccessGreen
                        "BLOQUEADO" -> ErrorRed
                        "PERDIDO" -> Color.Gray
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (!isReadOnly) {
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text("Cambiar Estado")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Activo") },
                            onClick = { onStatusChange("ACTIVO"); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Inactivo") },
                            onClick = { onStatusChange("INACTIVO"); expanded = false }
                        )
                         DropdownMenuItem(
                            text = { Text("Bloqueado") },
                            onClick = { onStatusChange("BLOQUEADO"); expanded = false }
                        )
                         DropdownMenuItem(
                            text = { Text("Perdido") },
                            onClick = { onStatusChange("PERDIDO"); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable // Added wrapper for DropdownMenu to avoid unresolved reference in some compose versions/contexts without full imports
fun DropdownMenu(expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, content = { content() })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSensorDialog(
    users: List<UserDto>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Llavero") }
    var selectedStatus by remember { mutableStateOf("ACTIVO") }
    var selectedUser by remember { mutableStateOf<UserDto?>(null) }
    var expandedUser by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

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
                
                // Tipo Sensor
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
                
                // Estado Inicial
                 ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado Inicial") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        listOf("ACTIVO", "INACTIVO", "BLOQUEADO", "PERDIDO").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    selectedStatus = status
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }
                
                // Usuario Dueño
                ExposedDropdownMenuBox(
                    expanded = expandedUser,
                    onExpandedChange = { expandedUser = !expandedUser }
                ) {
                    OutlinedTextField(
                        value = selectedUser?.name ?: "Seleccionar Dueño",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUser) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUser,
                        onDismissRequest = { expandedUser = false }
                    ) {
                        users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.name} ${user.lastName ?: ""}") },
                                onClick = {
                                    selectedUser = user
                                    expandedUser = false
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
                        onConfirm(code, selectedType, selectedUser!!.id, selectedStatus)
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
            isReadOnly = false,
            onBack = {},
            onCreateSensor = { _, _, _, _ -> },
            onStatusChange = { _, _ -> }
        )
    }
}
