package com.example.practica.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.practica.screen.DeveloperScreen
import com.example.practica.screen.ForgotPasswordScreen
import com.example.practica.screen.HomeScreen
import com.example.practica.screen.LoginScreen
import com.example.practica.screen.RegisterScreen
import com.example.practica.screen.ResetPasswordScreen
import com.example.practica.screen.SensorsScreen
import com.example.practica.screen.UserManagementScreen
import com.example.practica.screen.history.HistoryScreen
import com.example.practica.screen.led.LedControlScreen
import com.example.practica.screen.login.AuthState
import com.example.practica.screen.login.AuthViewModel
import com.example.practica.screen.user_management.DepartmentManagementScreen
import com.example.practica.screen.user_management.RfidManagementScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavGraph(vm: AuthViewModel = viewModel()) {
    val nav = rememberNavController()
    val authState by vm.authState.collectAsState()
    
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            LaunchedEffect(authState) {
                delay(3000)
                when (authState) {
                    AuthState.Checking -> {}
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
            SplashLottie()
        }
        composable(Route.Login.path) { LoginScreen(nav = nav, vm = vm) }
        composable(Route.Home.path) {
            HomeScreen(
                nav = nav,
                authVm = vm,
                onLogoutDone = {
                    vm.logout()
                    nav.navigate(Route.Login.path) { popUpTo(Route.Home.path) { inclusive = true } }
                }
            )
        }
        composable(Route.Register.path) { RegisterScreen(nav, vm) }
        composable(Route.ForgotPassword.path) { ForgotPasswordScreen(nav, vm) }
        composable(Route.ResetPassword.path) { ResetPasswordScreen(nav, vm) }
        composable(Route.UserManagement.path) {
            val currentUserId = (authState as? AuthState.Authenticated)?.user?.id
            UserManagementScreen(nav, currentUserId = currentUserId)
        }
        composable(Route.Sensors.path) { SensorsScreen(nav) }
        composable(Route.Developer.path) { DeveloperScreen(nav) }
        composable(Route.LedControl.path) { LedControlScreen(onBackClick = { nav.popBackStack() }) }
        
        // RfidManagement con argumentos opcionales
        composable(
            route = "${Route.RfidManagement.path}?readOnly={readOnly}&userId={userId}",
            arguments = listOf(
                navArgument("readOnly") { type = NavType.BoolType; defaultValue = false },
                navArgument("userId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val readOnly = backStackEntry.arguments?.getBoolean("readOnly") ?: false
            val userIdArg = backStackEntry.arguments?.getInt("userId") ?: -1
            val filterUserId = if (userIdArg != -1) userIdArg else null
            
            RfidManagementScreen(nav, isReadOnly = readOnly, filterUserId = filterUserId)
        }
        
        composable(Route.DepartmentManagement.path) { DepartmentManagementScreen(nav) }
        
        // Historial con parámetro opcional userId (si es -1 o no se pasa, asumimos "todos" para admin o logueado)
        // Usaremos: history/{userId}
        composable(
            route = "${Route.History.path}?userId={userId}",
            arguments = listOf(navArgument("userId") { 
                type = NavType.IntType 
                defaultValue = -1 
            })
        ) { backStackEntry ->
            val argId = backStackEntry.arguments?.getInt("userId") ?: -1
            val targetId = if (argId == -1) null else argId
            
            // Obtenemos el departmentId del usuario logueado para pasarlo a HistoryScreen
            val currentUser = (authState as? AuthState.Authenticated)?.user
            val myDeptId = currentUser?.departmentId ?: 1 // Default a 1 si no hay usuario (caso raro)

            HistoryScreen(nav, userId = targetId, departmentId = myDeptId)
        }
    }
}

@Composable
fun SplashLottie() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.example.practica.R.raw.loading_lottie)
    )
    val animState = animateLottieCompositionAsState(
        composition,
        iterations = Int.MAX_VALUE
    )
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
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
