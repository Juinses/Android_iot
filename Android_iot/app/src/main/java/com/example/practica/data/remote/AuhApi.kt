package com.example.practica.data.remote

import com.example.practica.data.remote.dto.ForgotPasswordRequest
import com.example.practica.data.remote.dto.ForgotPasswordResponse
import com.example.practica.data.remote.dto.LoginRequest
import com.example.practica.data.remote.dto.LoginResponse
import com.example.practica.data.remote.dto.RegisterRequest
import com.example.practica.data.remote.dto.RegisterResponse
import com.example.practica.data.remote.dto.ResetPasswordRequest
import com.example.practica.data.remote.dto.ResetPasswordResponse
import com.example.practica.data.remote.dto.UserActionResponse
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
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
    @GET("profile")
    suspend fun profile(
        @Header("Authorization") auth: String
    ): UserDto

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ResetPasswordResponse

    // --- CRUD USUARIOS (Rutas actualizadas a /admin/users) ---
    @GET("admin/users")
    suspend fun getUsers(): UserListResponse  // Ahora devuelve objeto { success, users }

    @POST("admin/users")
    suspend fun createUser(@Body body: RegisterRequest): UserActionResponse 
    // Devuelve { success, user }

    @PUT("admin/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body body: UserDto): UserActionResponse
    // Devuelve { success, message }

    @DELETE("admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): UserActionResponse
    // Devuelve { success, message }
}