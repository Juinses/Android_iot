package com.example.practica.data.remote.dto

data class UserListResponse(
    val success: Boolean,
    val users: List<UserDto>
)

data class UserActionResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserDto? = null
)