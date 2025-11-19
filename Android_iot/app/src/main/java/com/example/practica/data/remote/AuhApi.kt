package com.example.practica.data.remote

import com.example.practica.data.remote.dto.LoginRequest
import com.example.practica.data.remote.dto.LoginResponse
import com.example.practica.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login") // o "/auth/login" según cómo definiste BASE_URL
    suspend fun login(@Body body: LoginRequest): LoginResponse
    @GET("profile")
    suspend fun profile(
        @Header("Authorization") auth: String
    ): UserDto
}