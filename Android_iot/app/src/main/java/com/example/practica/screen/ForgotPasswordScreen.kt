package com.example.practica.screen

import android.os.CountDownTimer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.practica.R
import com.example.practica.nav.Route
import com.example.practica.screen.login.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    
    var isCodeSent by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    var screenMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    DisposableEffect(isTimerRunning) {
        var timer: CountDownTimer? = null
        if (isTimerRunning) {
            timer = object : CountDownTimer(timeLeft * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = (millisUntilFinished / 1000).toInt()
                }
                override fun onFinish() {
                    isTimerRunning = false
                    timeLeft = 0
                }
            }.start()
        }
        onDispose { timer?.cancel() }
    }

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
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "RECUPERAR CONTRASEÑA", 
            fontSize = 18.sp, 
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Campo Email Estilizado
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("INGRESE EMAIL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isCodeSent,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Recuperar (Color Primario / Morado)
        Button(
            onClick = {
                screenMessage = null
                isError = false
                
                if (email.isBlank()) {
                    screenMessage = "Email no puede estar vacío"
                    isError = true
                    return@Button
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    screenMessage = "Formato de email inválido"
                    isError = true
                    return@Button
                }
                
                isLoading = true
                vm.forgotPassword(
                    email = email,
                    onSuccess = { msg ->
                        isLoading = false
                        isCodeSent = true
                        timeLeft = 60
                        isTimerRunning = true
                        screenMessage = "Código enviado"
                        isError = false
                        Toast.makeText(context, "Correo enviado", Toast.LENGTH_SHORT).show()
                    },
                    onFail = { err ->
                        isLoading = false
                        screenMessage = err
                        isError = true
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !isLoading && !isCodeSent
        ) {
            Text(
                if (isLoading) "ENVIANDO..." else "RECUPERAR", 
                color = MaterialTheme.colorScheme.onPrimary, 
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Sección Código
        Text(
            "INGRESE CÓDIGO", 
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Campo Código
        OutlinedTextField(
            value = codeInput,
            onValueChange = { 
                if (it.length <= 5 && it.all { char -> char.isDigit() }) {
                    codeInput = it 
                    if (it.length == 5) {
                        if (!isCodeSent) {
                            screenMessage = "Primero solicite el código"
                            isError = true
                        } else if (timeLeft == 0) {
                            screenMessage = "Código vencido"
                            isError = true
                            isCodeSent = false 
                        } else {
                             vm.tempCodeForReset = it
                             nav.navigate(Route.ResetPassword.path)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 12.sp, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isCodeSent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { 
                Text("00000", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, letterSpacing = 12.sp, color = Color.LightGray) 
            },
            enabled = isCodeSent
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isCodeSent) {
            Text(
                text = "$timeLeft Segundos",
                color = if (timeLeft > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        if (screenMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = screenMessage!!,
                color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF008000),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(1f)) 
        
        Button(
            onClick = { nav.popBackStack() },
            colors = ButtonDefaults.textButtonColors(),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Volver", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}