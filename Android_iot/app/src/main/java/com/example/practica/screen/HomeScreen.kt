package com.example.practica.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.data.remote.dto.AccessEventDto
import com.example.practica.data.remote.dto.UserDto
import com.example.practica.nav.Route
import com.example.practica.screen.home.AccessState
import com.example.practica.screen.home.HomeViewModel
import com.example.practica.screen.login.AuthState
import com.example.practica.screen.login.AuthViewModel
import com.example.practica.ui.theme.ErrorRed
import com.example.practica.ui.theme.PracticaTheme
import com.example.practica.ui.theme.SuccessGreen
import com.example.practica.ui.theme.WarningOrange
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomeScreen(
    nav: NavController? = null,
    authVm: AuthViewModel = viewModel(),
    homeVm: HomeViewModel = viewModel(),
    onLogoutDone: () -> Unit
) {
    val authState by authVm.authState.collectAsState()
    val accessState by homeVm.accessState.collectAsState()
    val isOnline by homeVm.isOnline.collectAsState()
    val lastEvent by homeVm.lastEvent.collectAsState()
    val barrierStatus by homeVm.barrierStatus.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            onLogoutDone()
        }
    }

    LaunchedEffect(accessState) {
        when (val state = accessState) {
            is AccessState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                homeVm.resetState()
            }
            is AccessState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                homeVm.resetState()
            }
            else -> {}
        }
    }

    val user = (authState as? AuthState.Authenticated)?.user

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !isOnline,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarningOrange)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sin conexión a Internet - Modo Offline", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        HomeContent(
            user = user,
            accessState = accessState,
            isOnline = isOnline,
            lastEvent = lastEvent,
            barrierStatus = barrierStatus,
            onLogout = { authVm.logout() },
            onNavigate = { route -> nav?.navigate(route) },
            onOpenBarrier = { isDigitalKey -> 
                user?.let { homeVm.openBarrier(it.id, isDigitalKey) } 
            },
            onCloseBarrier = { 
                user?.let { homeVm.closeBarrier(it.id) } 
            }
        )
    }
}

@Composable
fun HomeContent(
    user: UserDto?,
    accessState: AccessState,
    isOnline: Boolean,
    lastEvent: AccessEventDto?,
    barrierStatus: String,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenBarrier: (Boolean) -> Unit,
    onCloseBarrier: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    
    // Simulación simple para Preview si user es null o contexto testing
    LaunchedEffect(Unit) {
        while (true) {
            val locale = Locale("es", "ES")
            val timeFormat = SimpleDateFormat("HH:mm", locale)
            val dateFormat = SimpleDateFormat("EEEE, d MMM", locale)
            // Fix para previews donde TimeZone puede fallar
            try {
                val zone = TimeZone.getTimeZone("America/Santiago")
                timeFormat.timeZone = zone
                dateFormat.timeZone = zone
            } catch (e: Exception) { /* Ignorar en preview */ }
            
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now).replaceFirstChar { it.uppercase() }
            delay(1000)
        }
    }

    val isAdmin = user?.role?.uppercase() == "ADMIN"
    val isLoading = accessState is AccessState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentDate.ifEmpty { "Cargando..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = currentTime.ifEmpty { "--:--" },
                    style = MaterialTheme.typography.displaySmall, // Más grande y moderno
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hola, ${user?.name ?: "Usuario"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .background(ErrorRed.copy(alpha = 0.1f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Salir",
                    tint = ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        if (lastEvent != null) {
            LastEventCard(lastEvent)
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (isAdmin) {
            AdminDashboard(
                isLoading = isLoading,
                isOnline = isOnline,
                barrierStatus = barrierStatus,
                onNavigate = onNavigate,
                onOpen = { onOpenBarrier(false) }, 
                onClose = onCloseBarrier
            )
        } else {
            OperatorDashboard(
                isLoading = isLoading,
                isOnline = isOnline,
                userId = user?.id ?: -1,
                onDigitalKey = { onOpenBarrier(true) }, 
                onNavigate = onNavigate
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "IoT Access Control v1.0",
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray,
            modifier = Modifier.clickable { onNavigate(Route.Developer.path) }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LastEventCard(event: AccessEventDto) {
    val isAllowed = event.result == "PERMITIDO"
    val color = if (isAllowed) SuccessGreen else ErrorRed
    val icon = if (isAllowed) Icons.Default.Check else Icons.Default.Close
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ÚLTIMA ACTIVIDAD", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.timestamp.substringAfter(" "), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon, 
                        contentDescription = null, 
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isAllowed) "Acceso Permitido" else "Acceso Denegado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = event.userName ?: "Usuario Desconocido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDashboard(
    isLoading: Boolean,
    isOnline: Boolean,
    barrierStatus: String,
    onNavigate: (String) -> Unit,
    onOpen: () -> Unit,
    onClose: () -> Unit
) {
    val isOpen = barrierStatus == "ABIERTA"
    val statusColor = if (isOpen) SuccessGreen else Color.Gray
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, if (isOpen) SuccessGreen.copy(alpha = 0.5f) else Color.LightGray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ESTADO DE BARRERA", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = barrierStatus,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
        
        Text("Control Manual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onOpen,
                enabled = isOnline && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen), 
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SUBIR", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onClose,
                enabled = isOnline && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), 
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("BAJAR", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("Gestión", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))

        MenuCard(
            icon = Icons.Default.Settings, 
            title = "Sensores RFID",
            subtitle = "Tarjetas y llaveros",
            onClick = { onNavigate(Route.RfidManagement.path) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        MenuCard(
            icon = Icons.Default.Person,
            title = "Residentes",
            subtitle = "Usuarios del sistema",
            onClick = { onNavigate(Route.UserManagement.path) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        MenuCard(
            icon = Icons.Default.History, 
            title = "Historial Completo",
            subtitle = "Bitácora de accesos",
            onClick = { onNavigate(Route.History.path) }
        )
    }
}

@Composable
fun OperatorDashboard(
    isLoading: Boolean, 
    isOnline: Boolean,
    userId: Int,
    onDigitalKey: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val buttonColors = if (!isOnline) 
        listOf(Color.Gray, Color.DarkGray) 
    else if (isLoading) 
        listOf(Color.Gray, Color.DarkGray)
    else 
        listOf(SuccessGreen, Color(0xFF1B5E20))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isOnline) "Toca para abrir" else "Sin Conexión", 
            style = MaterialTheme.typography.labelMedium, 
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(brush = Brush.verticalGradient(colors = buttonColors))
                .clickable(enabled = isOnline && !isLoading) { onDigitalKey() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (!isOnline) Icons.Default.Warning else if (isLoading) Icons.Default.Settings else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (!isOnline) "OFFLINE" else if (isLoading) "..." else "ABRIR",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionIcon(
                icon = Icons.Default.History,
                label = "Historial",
                onClick = { onNavigate("${Route.History.path}?userId=$userId") }
            )
            
             QuickActionIcon(
                icon = Icons.Default.Notifications,
                label = "Ambiente",
                onClick = { onNavigate(Route.Sensors.path) }
            )
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
            .height(68.dp) 
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(0.5.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun QuickActionIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(60.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

// --- PREVIEWS ---

@Preview(name = "Admin Dashboard", showBackground = true)
@Composable
fun HomeAdminPreview() {
    PracticaTheme {
        HomeContent(
            user = UserDto(1, "Juan Admin", "Pérez", "admin@test.com", "ADMIN"),
            accessState = AccessState.Idle,
            isOnline = true,
            lastEvent = AccessEventDto(99, "14:05", 2, "Maria Operador", "ACCESO PERMITIDO", "RFID", "PERMITIDO"),
            barrierStatus = "CERRADA",
            onLogout = {},
            onNavigate = {},
            onOpenBarrier = {},
            onCloseBarrier = {}
        )
    }
}

@Preview(name = "Operator Dashboard", showBackground = true)
@Composable
fun HomeOperatorPreview() {
    PracticaTheme {
        HomeContent(
            user = UserDto(2, "Carlos User", "Gomez", "user@test.com", "OPERADOR"),
            accessState = AccessState.Idle,
            isOnline = true,
            lastEvent = null, // Sin evento reciente
            barrierStatus = "CERRADA",
            onLogout = {},
            onNavigate = {},
            onOpenBarrier = {},
            onCloseBarrier = {}
        )
    }
}

@Preview(name = "Offline Mode", showBackground = true)
@Composable
fun HomeOfflinePreview() {
    PracticaTheme {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(WarningOrange).padding(8.dp),
                contentAlignment = Alignment.Center
            ) { Text("Sin conexión - Modo Offline", color = Color.White) }
            
            HomeContent(
                user = UserDto(1, "Juan Admin", "", "", "ADMIN"),
                accessState = AccessState.Error("Sin red"),
                isOnline = false,
                lastEvent = null,
                barrierStatus = "CERRADA",
                onLogout = {},
                onNavigate = {},
                onOpenBarrier = {},
                onCloseBarrier = {}
            )
        }
    }
}
