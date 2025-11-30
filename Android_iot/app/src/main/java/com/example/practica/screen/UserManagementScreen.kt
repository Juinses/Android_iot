package com.example.practica.screen

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.data.remote.dto.UserDto
import com.example.practica.screen.user_management.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(nav: NavController, vm: UserManagementViewModel = viewModel()) {
    val state by vm.uiState
    var searchQuery by remember { mutableStateOf("") }
    
    // Estados para diálogos
    var showUserDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserDto?>(null) } // Si es null, es crear nuevo
    
    val context = LocalContext.current

    // Manejo de mensajes (Toast)
    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.clearMessages()
            showUserDialog = false
            showDeleteDialog = false
        }
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    // Botón Volver si se desea
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedUser = null // Modo Crear
                showUserDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    vm.filterUsers(it)
                },
                label = { Text("Buscar por nombre o email") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(state.filteredUsers) { user ->
                        UserItem(
                            user = user,
                            onEdit = {
                                selectedUser = user
                                showUserDialog = true
                            },
                            onDelete = {
                                selectedUser = user
                                showDeleteDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (state.filteredUsers.isEmpty()) {
                        item {
                            Text(
                                "No se encontraron usuarios.",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // DIÁLOGO CREAR / EDITAR
    if (showUserDialog) {
        UserFormDialog(
            user = selectedUser,
            onDismiss = { showUserDialog = false },
            onConfirm = { name, lastName, email, pass ->
                if (selectedUser == null) {
                    // Crear
                    vm.createUser(name, lastName, email, pass)
                } else {
                    // Editar (La contraseña no se actualiza aquí por simplicidad, o es opcional)
                    // backend update usually doesn't need pass unless changing it.
                    // Asumiremos update de datos básicos
                    val updatedUser = selectedUser!!.copy(
                        name = name,
                        lastName = lastName,
                        email = email
                    )
                    vm.updateUser(updatedUser)
                }
            }
        )
    }

    // DIÁLOGO CONFIRMAR ELIMINAR
    if (showDeleteDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de eliminar a ${selectedUser!!.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteUser(selectedUser!!.id)
                }) {
                    Text("Sí, eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserItem(
    user: UserDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.name} ${user.lastName ?: ""}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = user.email, fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Blue)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

@Composable
fun UserFormDialog(
    user: UserDto?, // Si es null, es crear
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var password by remember { mutableStateOf("") } // Solo obligatoria al crear
    var confirmPassword by remember { mutableStateOf("") }
    
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Nuevo Usuario" else "Editar Usuario") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellido") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                
                // Password solo visible al crear (o si quisieras cambiarla al editar, pero simplificaremos)
                if (user == null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirmar pass") })
                }

                if (localError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(localError!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                localError = null
                // Validaciones básicas
                if (name.isBlank() || lastName.isBlank() || email.isBlank()) {
                    localError = "Campos obligatorios vacíos"
                    return@Button
                }
                if (user == null) {
                    // Validaciones extra al crear
                    if (password.isBlank()) {
                        localError = "Contraseña obligatoria"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        localError = "Contraseñas no coinciden"
                        return@Button
                    }
                    // Validación robustez pass (opcional según rúbrica aquí también)
                    if (password.length < 6) { // Ejemplo simple
                        localError = "Contraseña muy corta"
                        return@Button
                    }
                }
                
                onConfirm(name, lastName, email, password)
            }) {
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