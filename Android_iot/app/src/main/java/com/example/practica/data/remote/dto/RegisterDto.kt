package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class RegisterRequest(
    val name: String,
    @Json(name = "last_name") val lastName: String,
    val email: String,
    val password: String,
    @Json(name = "rol") val role: String? = "OPERADOR",
    @Json(name = "id_departamento") val departmentId: Int? = null
)

data class RegisterResponse(
    val message: String
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