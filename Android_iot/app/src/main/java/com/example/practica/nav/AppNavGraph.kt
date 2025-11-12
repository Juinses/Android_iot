package com.example.practica.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.practica.screen.HomeScreen
import com.example.practica.screen.LoginScreen
import com.example.practica.screen.RegisterScreen

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Login.path) {
        composable(Route.Login.path) { LoginScreen(nav) }
        composable(Route.Register.path) { RegisterScreen(nav) }
        composable(Route.Home.path) { HomeScreen() }
    }
}