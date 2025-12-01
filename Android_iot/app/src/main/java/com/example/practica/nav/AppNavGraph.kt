package com.example.practica.nav

import com.example.practica.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.practica.screen.HomeScreen
import com.example.practica.screen.LoginScreen
import com.example.practica.screen.RegisterScreen
import com.example.practica.screen.ForgotPasswordScreen
import com.example.practica.screen.ResetPasswordScreen
import com.example.practica.screen.UserManagementScreen
import com.example.practica.screen.SensorsScreen
import com.example.practica.screen.DeveloperScreen
import com.example.practica.screen.login.AuthState
import com.example.practica.screen.login.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun AppNavGraph(vm: AuthViewModel = viewModel()) {
    val nav = rememberNavController()
    val authState by vm.authState.collectAsState()
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            // AQUÍ reaccionamos a los cambios de authState
            LaunchedEffect(authState) {
                // Retraso artificial de 3 segundos para ver el Splash (requisito UX)
                delay(3000)
                
                when (authState) {
                    AuthState.Checking -> {
                        // Sigue chequeando, pero si pasa el tiempo y sigue aquí, podría haber error.
                        // Normalmente Checking dura milisegundos.
                    }
                    is AuthState.Authenticated -> {
                        nav.navigate(Route.Home.path) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    AuthState.Unauthenticated,
                    is AuthState.Error -> {
                        nav.navigate(Route.Login.path) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }
            // Solo dibuja la animación
            SplashLottie()
        }
        composable(Route.Login.path) {
            LoginScreen(
                nav = nav,
                vm = vm
            )
        }
        composable(Route.Home.path) {
            HomeScreen(
                nav = nav,
                onLogoutDone = {
                    vm.logout()
                    nav.navigate(Route.Login.path) {
                        popUpTo(Route.Home.path) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Register.path) {
            RegisterScreen(nav, vm)
        }
        composable(Route.ForgotPassword.path) {
            ForgotPasswordScreen(nav, vm)
        }
        composable(Route.ResetPassword.path) {
            ResetPasswordScreen(nav, vm)
        }
        composable(Route.UserManagement.path) {
            // Obtenemos el ID del usuario actual si está autenticado
            val currentUserId = (authState as? AuthState.Authenticated)?.user?.id
            UserManagementScreen(nav, currentUserId = currentUserId)
        }
        composable(Route.Sensors.path) {
            SensorsScreen(nav)
        }
        composable(Route.Developer.path) {
            DeveloperScreen(nav)
        }
    }
}
@Composable
fun SplashLottie() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading_lottie)
    )
    val animState = animateLottieCompositionAsState(
        composition,
        iterations = Int.MAX_VALUE
    )
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), // Fondo del tema
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (composition == null) {
                CircularProgressIndicator()
            } else {
                LottieAnimation(
                    composition = composition,
                    progress = { animState.progress },
                    modifier = Modifier.size(250.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Practica IoT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cargando recursos...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}