package com.example.practica.nav

sealed class Route(val path: String) {
    object Login : Route("login")
    object Home : Route("home")
    object Register : Route("register")
    object ForgotPassword : Route("forgot_password")
    object ResetPassword : Route("reset_password")
    object UserManagement : Route("user_management")
    object Sensors : Route("sensors")
    object Developer : Route("developer")
    object LedControl : Route("led_control")
    object RfidManagement : Route("rfid_management")
    object History : Route("history")
    
    // Nueva ruta para departamentos
    object DepartmentManagement : Route("department_management")
}
