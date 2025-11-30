package com.example.practica.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.nav.Route
import com.example.practica.screen.login.AuthViewModel

@Composable
fun ResetPasswordScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Restablecer Contraseña", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(16.dp))
        
        Text(
            "Ingresa el código que recibiste por correo y tu nueva contraseña.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (Confírmalo)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Código de 5 dígitos") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = newPass,
            onValueChange = { newPass = it },
            label = { Text("Nueva contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        if (localError != null) {
            Spacer(Modifier.height(8.dp))
            Text(localError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                localError = null
                if (email.isBlank() || code.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
                    localError = "Todos los campos son obligatorios"
                } else if (newPass != confirmPass) {
                    localError = "Las contraseñas no coinciden"
                } else if (!isPasswordRobustReset(newPass)) {
                    localError = "Contraseña débil: mín 8 chars, 1 Mayús, 1 minús, 1 num, 1 símbolo."
                } else {
                    vm.resetPassword(
                        email = email,
                        code = code,
                        newPass = newPass,
                        onSuccess = { msg ->
                            Toast.makeText(context, "Contraseña cambiada: $msg", Toast.LENGTH_LONG).show()
                            // Volver al Login
                            nav.navigate(Route.Login.path) {
                                popUpTo(Route.Login.path) { inclusive = true }
                            }
                        },
                        onFail = { err ->
                            localError = err
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cambiar Contraseña")
        }
    }
}

// Función auxiliar para validar robustez (copiada para asegurar consistencia)
fun isPasswordRobustReset(password: String): Boolean {
    if (password.length < 8) return false
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    return hasUpper && hasLower && hasDigit && hasSpecial
}