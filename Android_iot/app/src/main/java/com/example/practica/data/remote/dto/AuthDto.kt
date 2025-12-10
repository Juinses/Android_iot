package com.example.practica.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String,
    val user: UserDto
)

data class RegisterRequest(
    val name: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String = "OPERATOR", // Default role
    val departmentId: Int = 0 // Default department
)