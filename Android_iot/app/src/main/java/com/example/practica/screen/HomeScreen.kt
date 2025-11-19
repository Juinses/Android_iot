package com.example.practica.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.practica.data.remote.dto.UserDto
import com.example.practica.screen.login.AuthState
import com.example.practica.screen.login.AuthViewModel
import com.example.practica.ui.theme.PracticaTheme

//screens/HomeScreen.kt
@Composable
fun LoginContent(authState: AuthState, onLogout: () -> Unit ) {
    Column( modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar datos del usuario si están disponibles
        if (authState is AuthState.Authenticated) {
            val user = (authState as AuthState.Authenticated).user
            Text("Bienvenido ${user.name}", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))
        }
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
@Composable
fun HomeScreen(
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
    LoginContent(
        authState = authState,
        onLogout = { vm.logout() }
    )
}
@Composable
fun HomeContentPreviewUI(
    authState: AuthState,
    onLogout: () -> Unit = {}
) {
    LoginContent(
        authState = authState,
        onLogout = onLogout
    )
}
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AppIotComposeTheme {
        HomeContentPreviewUI(
            authState = AuthState.Authenticated(
                user = UserDto(
                    id = 1,
                    name = "Javier Ahumada",
                    email = "javier@example.com"
                )
            )
        )
    }
}