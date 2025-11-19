package com.example.practica.data

import android.content.Context
import com.example.practica.data.local.TokenStorage
import com.example.practica.data.remote.AuthApi
import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.LoginRequest
import com.example.practica.data.remote.dto.LoginResponse
import com.example.practica.data.remote.dto.UserDto

class AuthRepository(
    private val api: AuthApi = HttpClient.authApi
) {
    // ---------- LOGIN ----------
    suspend fun login(ctx: Context, email: String, password: String): Result<LoginResponse> {
        return try {
            // 1. Construimos el body correcto
            val body = LoginRequest(email = email, password = password)
            // 2. Llamamos a la API
            val response = api.login(body)
            // 3. Validamos success == true
            if (!response.success) {
                return Result.failure(Exception("Credenciales inválidas"))
            }
            // 4. Guardamos el token en DataStore
            TokenStorage.saveToken(ctx, response.token)
            // 5. Devolvemos el paquete completo (token + user)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // ---------- TOKEN ----------
    suspend fun getStoredToken(ctx: Context): String? {
        return TokenStorage.getToken(ctx)
    }
    suspend fun clearToken(ctx: Context) {
        TokenStorage.clearToken(ctx)
    }
    // ---------- VALIDAR TOKEN (GET /profile) ----------
    suspend fun validateToken(ctx: Context): Result<UserDto> {
        return try {
            val token = getStoredToken(ctx)
                ?: return Result.failure(Exception("Sin token guardado"))
            val user = api.profile("Bearer $token")
            // Aquí valida contra backend
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}