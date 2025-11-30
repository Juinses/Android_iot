package com.example.practica.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

// UserDto eliminado de aquí porque ya existe en UserDto.kt

data class LoginResponse(
    val success: Boolean,
    val token: String,
    val user: UserDto
)