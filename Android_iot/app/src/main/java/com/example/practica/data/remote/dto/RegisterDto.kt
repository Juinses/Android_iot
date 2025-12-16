package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

// Para registro y creación
data class RegisterRequest(
    val name: String,
    @Json(name = "last_name") val lastName: String,
    val email: String,
    val password: String, // Ahora será opcional para actualización, pero obligatorio para create
    @Json(name = "departamento_id") val departmentId: Int? = null, // Puede no ir en update
    @Json(name = "rol") val role: String? = null
)

// DTO específico para actualizar usuario (sin password, sin departamento_id obligatorio)
data class UpdateUserRequest(
    val name: String? = null,
    @Json(name = "last_name") val lastName: String? = null,
    val email: String? = null
)

data class RegisterResponse(
    val message: String,
    val user: UserDto? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    @Json(name = "new_password") val newPassword: String
)

data class ResetPasswordResponse(
    val message: String
)
