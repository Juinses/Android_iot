package com.example.practica.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.practica.R
import com.example.practica.nav.Route
import com.example.practica.screen.login.AuthState
import com.example.practica.screen.login.AuthViewModel
import com.example.practica.ui.theme.PracticaTheme

@Composable
fun LoginContent(email: String, pass: String, authState: AuthState, onEmailChange: (String) ->
Unit,
                 onPassChange: (String) -> Unit,
                 onLoginClick: () -> Unit,
                 onRegisterClick: () -> Unit,
                 onForgotClick: () -> Unit
) {
    val isLoading = authState is AuthState.Checking
    val errorMessage = (authState as? AuthState.Error)?.message
    // Variable para controlar si hay un error local (campos vacíos, email inválido)
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // nombre del archivo
            contentDescription = "Logo de la app",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 12.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text("Bienvenido",
            fontSize = 23.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField( email,
            onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = localError != null || errorMessage != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(pass,
            onPassChange,
            label = { Text("Contraseña") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = localError != null || errorMessage != null
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                localError = null // Limpiar errores previos
                if (email.isBlank() || pass.isBlank()) {
                    localError = "Campos obligatorios vacíos"
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    localError = "Formato de email inválido"
                } else {
                    onLoginClick()
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Ingresando..." else "Ingresar")
        }
        
        // Mostrar errores (Locales o del ViewModel)
        if (localError != null) {
            Spacer(Modifier.height(8.dp))
            Text(localError!!, color = MaterialTheme.colorScheme.error)
        }
        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
        
        // Sección de enlaces (Registro y Recuperar)
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            TextButton(onClick = onRegisterClick) {
                Text("¿No tienes cuenta? Regístrate")
            }
            // "Recuperar Contraseña" (Punto 2 de la guía)
            TextButton(onClick = onForgotClick) {
                Text("¿Olvidaste tu contraseña?", color = Color.Gray)
            }
        }
    }
}
@Composable
fun LoginScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val authState by vm.authState.collectAsState()
    
    // Si AuthState cambió a Authenticated → navega
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            nav.navigate(Route.Home.path) {
                popUpTo(Route.Login.path) { inclusive = true }
            }
        }
    }
    LoginContent(
        email, pass,
        authState = authState,
        onEmailChange = { email = it },
        onPassChange = { pass = it },
        onLoginClick = { vm.login(email.trim(), pass) },
        onRegisterClick = { nav.navigate(Route.Register.path) },
        onForgotClick = { nav.navigate(Route.ForgotPassword.path) }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginContentPreview() {
    PracticaTheme {
        LoginContent(
            email = "javier@demo.cl",
            pass = "123456",
            authState = AuthState.Unauthenticated,
            onEmailChange = {},
            onPassChange = {},
            onLoginClick = {},
            onRegisterClick = {},
            onForgotClick = {}
        )
    }
}