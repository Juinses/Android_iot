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
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    // --- AUTENTICACIÓN PÚBLICA ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ResetPasswordResponse

    // --- PERFIL ---
    // Backend: GET /profile -> UserDto
    @GET("profile")
    suspend fun profile(@Header("Authorization") token: String): UserDto

    // --- GESTIÓN DE USUARIOS (ADMIN) ---
    // Backend: GET /api/departamentos/:id/usuarios -> { data: [...] }
    @GET("api/departamentos/{deptId}/usuarios")
    suspend fun getUsers(@Path("deptId") departmentId: Int): UserListResponse

    // Backend: POST /api/departamentos/:id/usuarios
    @POST("api/departamentos/{deptId}/usuarios")
    suspend fun createUser(
        @Path("deptId") departmentId: Int,
        @Body request: RegisterRequest
    ): RegisterResponse

    // Backend: PATCH /api/usuarios/:id/estado
    @PATCH("api/usuarios/{id}/estado")
    suspend fun updateUserStatus(@Path("id") id: Int, @Body body: Map<String, String>): UserDto 

    // DELETE no estaba en el archivo usuarios.js, lo dejamos o comentamos si no existe
    @DELETE("api/usuarios/{id}")
    suspend fun deleteUser(@Path("id") id: Int): RegisterResponse
}
