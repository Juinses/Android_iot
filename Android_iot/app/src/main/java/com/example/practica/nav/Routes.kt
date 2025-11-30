package com.example.practica.nav

sealed class Route(val path: String) {
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
    data object ForgotPassword : Route("forgot_password")
    data object ResetPassword : Route("reset_password")
    data object UserManagement : Route("user_management")
    data object Sensors : Route("sensors")
    data object Developer : Route("developer")
}