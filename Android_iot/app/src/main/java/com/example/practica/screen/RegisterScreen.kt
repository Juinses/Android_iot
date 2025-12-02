package com.example.practica.screen

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.practica.nav.Route
import com.example.practica.screen.login.AuthViewModel
import com.example.practica.ui.theme.PracticaTheme

@Composable
fun RegisterScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    
    var localError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Crear cuenta", 
            fontSize = 22.sp, 
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = localError != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            isError = localError != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
            isError = localError != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = pwd,
            onValueChange = { pwd = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = localError != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPwd,
            onValueChange = { confirmPwd = it },
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = localError != null
        )
        
        if (localError != null) {
            Spacer(Modifier.height(8.dp))
            Text(localError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                localError = null
                
                // Validaciones Locales
                if (name.isBlank() || lastName.isBlank() || email.isBlank() || pwd.isBlank() || confirmPwd.isBlank()) {
                    localError = "Campos obligatorios vacíos"
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    localError = "Formato de email inválido"
                } else if (pwd != confirmPwd) {
                    localError = "Las contraseñas no coinciden"
                } else if (!isPasswordRobust(pwd)) {
                    localError = "Contraseña débil: debe tener al menos 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 símbolo."
                } else {
                    vm.register(
                        name = name,
                        lastName = lastName,
                        email = email,
                        pass = pwd,
                        onSuccess = {
                            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            nav.navigate(Route.Login.path) {
                                popUpTo(Route.Register.path) { inclusive = true }
                            }
                        },
                        onFail = { errorMsg ->
                            // Aquí podríamos mostrarlo en localError también si preferimos no usar Toast para errores de backend
                            localError = errorMsg 
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
        
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al Login")
        }
    }
}

// Función auxiliar para validar robustez
fun isPasswordRobust(password: String): Boolean {
    if (password.length < 8) return false
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    
    return hasUpper && hasLower && hasDigit && hasSpecial
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    PracticaTheme {
        RegisterScreen(nav = rememberNavController())
    }
}