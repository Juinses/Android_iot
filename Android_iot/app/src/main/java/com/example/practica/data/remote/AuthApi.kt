package com.example.practica.data.remote

import com.example.practica.data.remote.dto.ForgotPasswordRequest
import com.example.practica.data.remote.dto.ForgotPasswordResponse
import com.example.practica.data.remote.dto.LoginRequest
import com.example.practica.data.remote.dto.LoginResponse
import com.example.practica.data.remote.dto.RegisterRequest
import com.example.practica.data.remote.dto.RegisterResponse
import com.example.practica.data.remote.dto.ResetPasswordRequest
import com.example.practica.data.remote.dto.ResetPasswordResponse
import com.example.practica.data.remote.dto.UserDto
import com.example.practica.data.remote.dto.UserListResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {

    // --- AUTENTICACIÓN PÚBLICA ---
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ResetPasswordResponse

    // --- PERFIL ---
    @GET("api/auth/profile")
    suspend fun profile(@Header("Authorization") token: String): UserDto

    // --- GESTIÓN DE USUARIOS (ADMIN) ---
    // Asumimos endpoints RESTful estándar para usuarios
    
    @GET("api/users")
    suspend fun getUsers(): UserListResponse

    @POST("api/users")
    suspend fun createUser(@Body request: RegisterRequest): RegisterResponse

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UserDto): RegisterResponse // Usamos RegisterResponse por el mensaje genérico

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): RegisterResponse
}
