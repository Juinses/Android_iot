package com.example.practica.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
                 onRegisterClick: () -> Unit
) {
    val isLoading = authState is AuthState.Checking
    val errorMessage = (authState as? AuthState.Error)?.message
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField( email,
            onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(pass,
            onPassChange,
            label = { Text("Contraseña") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onLoginClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Ingresando..." else "Ingresar")
        }
        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
        TextButton(onClick = onRegisterClick, modifier = Modifier.align(Alignment.End)) {
            Text("¿No tienes cuenta? Regístrate")
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
        onRegisterClick = { nav.navigate(Route.Register.path) }
    )
}
@Preview(showBackground = true)
@Composable
fun LoginContentPreview() {
    AppIotComposeTheme {
        LoginContent(
            email = "javier@demo.cl",
            pass = "123456",
            authState = AuthState.Unauthenticated,
            onEmailChange = {},
            onPassChange = {},
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}

@Composable
fun AppIotComposeTheme(content: @Composable () -> Unit) {
    TODO("Not yet implemented")
}