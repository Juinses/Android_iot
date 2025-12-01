package com.example.practica.screen

import android.widget.Toast
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.R
import com.example.practica.nav.Route
import com.example.practica.screen.login.AuthViewModel

@Composable
fun ResetPasswordScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    // Recuperar email y código desde el ViewModel compartido
    val email = vm.tempEmailForReset
    val code = vm.tempCodeForReset
    
    // Estados del formulario
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "CREAR CONTRASEÑAS", 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Campo Nueva Clave
        OutlinedTextField(
            value = newPass,
            onValueChange = { newPass = it },
            label = { Text("INGRESE CLAVE") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(Modifier.height(16.dp))

        // Campo Repetir Clave
        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            label = { Text("REPETIR CLAVE") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Mensajes de error
        if (localError != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = localError!!, 
                color = MaterialTheme.colorScheme.error, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else if (email == null || code == null) {
             Spacer(Modifier.height(16.dp))
             Text(
                 text = "Error de sesión: Vuelva a solicitar el código.",
                 color = MaterialTheme.colorScheme.error,
                 fontWeight = FontWeight.Bold,
                 textAlign = androidx.compose.ui.text.style.TextAlign.Center
             )
        }
        
        // Ayuda visual
        if (newPass.isNotEmpty() && !isPasswordRobustReset(newPass)) {
             Spacer(Modifier.height(8.dp))
             Text(
                 text = "Debe tener 8+ caracteres, mayúscula, minúscula, número y símbolo.",
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                 textAlign = androidx.compose.ui.text.style.TextAlign.Center
             )
        }

        Spacer(Modifier.height(32.dp))

        // Botón CREAR (Morado)
        Button(
            onClick = {
                localError = null
                
                // Validaciones
                if (email == null || code == null) {
                    localError = "Falta información (email/código). Reinicie el proceso."
                    return@Button
                }
                if (newPass.isBlank() || confirmPass.isBlank()) {
                    localError = "Las contraseñas son obligatorias"
                    return@Button
                }
                if (newPass != confirmPass) {
                    localError = "Las contraseñas no coinciden"
                    return@Button
                }
                if (!isPasswordRobustReset(newPass)) {
                    localError = "La contraseña es débil"
                    return@Button
                }
                
                // Llamada REAL al servidor
                vm.resetPassword(
                    email = email,
                    code = code,
                    newPass = newPass,
                    onSuccess = { msg ->
                        Toast.makeText(context, "Contraseña cambiada correctamente", Toast.LENGTH_LONG).show()
                        // Navegar al Login
                        nav.navigate(Route.Login.path) {
                            popUpTo(Route.Login.path) { inclusive = true }
                        }
                    },
                    onFail = { err ->
                         localError = err
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = (email != null && code != null) // Bloquear si faltan datos
        ) {
            Text("CREAR", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

private fun isPasswordRobustReset(password: String): Boolean {
    if (password.length < 8) return false
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    return hasUpper && hasLower && hasDigit && hasSpecial
}