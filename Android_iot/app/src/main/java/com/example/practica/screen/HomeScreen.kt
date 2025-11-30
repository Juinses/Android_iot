package com.example.practica.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.data.remote.dto.UserDto
import com.example.practica.nav.Route
import com.example.practica.screen.login.AuthState
import com.example.practica.screen.login.AuthViewModel
import com.example.practica.ui.theme.PracticaTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    nav: NavController? = null, // Opcional para preview
    vm: AuthViewModel = viewModel(),
    onLogoutDone: () -> Unit
) {
    val authState by vm.authState.collectAsState()
    // Si después de logout el estado cambia a Unauthenticated, navegar
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            onLogoutDone()
        }
    }
    HomeContent(
        authState = authState,
        onLogout = { vm.logout() },
        onNavigate = { route -> nav?.navigate(route) }
    )
}

@Composable
fun HomeContent(
    authState: AuthState,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var currentTime by remember { mutableStateOf("") }

    // Reloj en tiempo real
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            currentTime = sdf.format(Date())
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Saludo y Reloj
        if (authState is AuthState.Authenticated) {
            val user = (authState as AuthState.Authenticated).user
            Text("Hola, ${user.name}", style = MaterialTheme.typography.headlineSmall)
        } else {
            Text("Bienvenido", style = MaterialTheme.typography.headlineSmall)
        }
        
        Spacer(Modifier.height(8.dp))
        Text(currentTime, style = MaterialTheme.typography.titleLarge)
        
        Spacer(Modifier.height(40.dp))

        // Botones del Menú
        MenuCard(
            icon = Icons.Default.Person, // Usando ícono básico
            title = "Gestión de Usuarios",
            subtitle = "CRUD de Usuarios",
            onClick = { onNavigate(Route.UserManagement.path) }
        )
        
        Spacer(Modifier.height(16.dp))
        
        MenuCard(
            icon = Icons.Default.Notifications, // Usando ícono básico
            title = "Sensores IOT",
            subtitle = "Ver datos de temperatura y luces",
            onClick = { onNavigate(Route.Sensors.path) }
        )

        Spacer(Modifier.height(16.dp))
        
        MenuCard(
            icon = Icons.Default.Info, // Usando ícono básico
            title = "Datos Desarrollador",
            subtitle = "Información del equipo",
            onClick = { onNavigate(Route.Developer.path) }
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun MenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PracticaTheme{
        HomeContent(
            authState = AuthState.Authenticated(
                user = UserDto(
                    id = 1,
                    name = "Usuario Demo",
                    email = "demo@example.com"
                )
            ),
            onLogout = {},
            onNavigate = {}
        )
    }
}