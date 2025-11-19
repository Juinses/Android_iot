package com.example.practica.data.remote.dto

data class LoginRequest(
    val email: String,
    val password: String
)
data class UserDto(
    val id: Int,
    val name: String,
    val email: String
)
data class LoginResponse(
    val success: Boolean,
    val token: String,
    val user: UserDto
)