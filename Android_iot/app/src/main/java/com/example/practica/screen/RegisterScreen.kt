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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.nav.Route
import com.example.practica.screen.login.AuthViewModel
import com.example.practica.ui.theme.PracticaTheme

@Composable
fun RegisterScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    var externalError by remember { mutableStateOf<String?>(null) }

    RegisterContent(
        externalError = externalError,
        onClearError = { externalError = null },
        onRegister = { name, lastName, email, pwd ->
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
                    externalError = errorMsg
                }
            )
        },
        onBack = { nav.popBackStack() }
    )
}

@Composable
fun RegisterContent(
    externalError: String? = null,
    onClearError: () -> Unit = {},
    onRegister: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    
    var localError by remember { mutableStateOf<String?>(null) }

    // Mostramos el error local (validación) o el externo (API), dando prioridad al local
    val displayError = localError ?: externalError

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
            onValueChange = { name = it; localError = null; onClearError() },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = displayError != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it; localError = null; onClearError() },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth(),
            isError = displayError != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localError = null; onClearError() },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
            isError = displayError != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = pwd,
            onValueChange = { pwd = it; localError = null; onClearError() },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = displayError != null
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPwd,
            onValueChange = { confirmPwd = it; localError = null; onClearError() },
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = displayError != null
        )
        
        if (displayError != null) {
            Spacer(Modifier.height(8.dp))
            Text(displayError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                localError = null
                onClearError()
                
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
                    onRegister(name, lastName, email, pwd)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }
        
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onBack,
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
        // En el preview usamos RegisterContent directamente con lambdas vacías
        // Esto evita instanciar el AuthViewModel que causa el "Render Problem"
        RegisterContent(
            onRegister = { _, _, _, _ -> },
            onBack = {}
        )
    }
}
