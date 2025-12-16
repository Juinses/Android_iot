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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.data.remote.dto.DepartmentDto
import com.example.practica.ui.theme.PracticaTheme

@Composable
fun DepartmentManagementScreen(
    nav: NavController,
    vm: DepartmentManagementViewModel = viewModel()
) {
    val state by vm.uiState
    val context = LocalContext.current
    
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

    DepartmentManagementContent(
        state = state,
        onBack = { nav.popBackStack() },
        onCreateDepartment = { number, tower, other -> 
            vm.createDepartment(number, tower, other) 
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentManagementContent(
    state: DepartmentUiState,
    onBack: () -> Unit,
    onCreateDepartment: (String, String, String?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión Departamentos") },
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
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Depto", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
            if (state.isLoading && state.departments.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.departments) { dept ->
                        DepartmentItem(dept)
                    }
                    if (state.departments.isEmpty()) {
                        item {
                            Text(
                                "No hay departamentos registrados.",
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
        AddDepartmentDialog(
            onDismiss = { showDialog = false },
            onConfirm = { num, tower, other ->
                onCreateDepartment(num, tower, other)
                showDialog = false
            }
        )
    }
}

@Composable
fun DepartmentItem(dept: DepartmentDto) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
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
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Depto: ${dept.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Torre: ${dept.tower}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!dept.otherData.isNullOrBlank()) {
                    Text(
                        text = dept.otherData,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "#${dept.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun AddDepartmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var tower by remember { mutableStateOf("") }
    var otherData by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Departamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Número (ej: 101)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = tower,
                    onValueChange = { tower = it },
                    label = { Text("Torre (ej: A)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = otherData,
                    onValueChange = { otherData = it },
                    label = { Text("Otros datos (opcional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (number.isNotBlank() && tower.isNotBlank()) {
                        onConfirm(number, tower, otherData.ifBlank { null })
                    }
                },
                enabled = number.isNotBlank() && tower.isNotBlank()
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
fun DepartmentPreview() {
    PracticaTheme {
        DepartmentManagementContent(
            state = DepartmentUiState(
                departments = listOf(
                    DepartmentDto(1, "101", "A", "Piso 1"),
                    DepartmentDto(2, "202", "B", null)
                )
            ),
            onBack = {},
            onCreateDepartment = { _, _, _ -> }
        )
    }
}
