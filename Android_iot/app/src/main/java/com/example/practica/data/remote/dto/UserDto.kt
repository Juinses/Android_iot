package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class UserDto(
    val id: Int,
    val name: String,
    @Json(name = "last_name") val lastName: String? = null,
    val email: String
)