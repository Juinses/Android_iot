package com.example.practica.data

import android.content.Context
import android.util.Log
import com.example.practica.data.local.TokenStorage
import com.example.practica.data.remote.AuthApi
import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.ForgotPasswordRequest
import com.example.practica.data.remote.dto.LoginRequest
import com.example.practica.data.remote.dto.LoginResponse
import com.example.practica.data.remote.dto.RegisterRequest
import com.example.practica.data.remote.dto.ResetPasswordRequest
import com.example.practica.data.remote.dto.UserDto

class AuthRepository(
    private val api: AuthApi = HttpClient.authApi
) {
    // ---------- LOGIN ----------
    suspend fun login(ctx: Context, email: String, password: String): Result<LoginResponse> {
        return try {
            Log.d("AuthRepository", "Intentando login con: $email")
            // 1. Construimos el body correcto
            val body = LoginRequest(email = email, password = password)
            // 2. Llamamos a la API
            val response = api.login(body)
            Log.d("AuthRepository", "Login respuesta: success=${response.success}")
            
            // 3. Validamos success == true
            if (!response.success) {
                return Result.failure(Exception("Credenciales inválidas"))
            }
            
            // VALIDACIÓN ADICIONAL DEL USUARIO
            val user = response.user
            if (user != null) {
                if (user.status == "INACTIVO" || user.status == "BLOQUEADO") {
                     return Result.failure(Exception("Usuario ${user.status}. Contacte al administrador."))
                }
            }
            
            // 4. Guardamos el token en DataStore
            TokenStorage.saveToken(ctx, response.token)
            // 5. Devolvemos el paquete completo (token + user)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en login", e)
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
            // Validar estado nuevamente
             if (user.status == "INACTIVO" || user.status == "BLOQUEADO") {
                 return Result.failure(Exception("Usuario ${user.status}. Contacte al administrador."))
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------- REGISTER ----------
    suspend fun register(name: String, lastName: String, email: String, password: String): Result<String> {
        return try {
            Log.d("AuthRepository", "Intentando registro: $name $lastName")
            // Argumentos nombrados para evitar errores
            val body = RegisterRequest(
                name = name, 
                lastName = lastName, 
                email = email, 
                password = password
            )
            val response = api.register(body)
            
            Log.d("AuthRepository", "Registro respuesta: message=${response.message}")

            Result.success(response.message)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en registro", e)
            Result.failure(e)
        }
    }

    // ---------- FORGOT PASSWORD ----------
    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val body = ForgotPasswordRequest(email)
            val response = api.forgotPassword(body)
            Result.success(response.message)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en forgotPassword", e)
            Result.failure(e)
        }
    }

    // ---------- RESET PASSWORD ----------
    suspend fun resetPassword(email: String, code: String, newPass: String): Result<String> {
        return try {
            val body = ResetPasswordRequest(email, code, newPass)
            val response = api.resetPassword(body)
            Result.success(response.message)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en resetPassword", e)
            Result.failure(e)
        }
    }

    // ---------- CRUD USUARIOS ----------
    suspend fun getUsers(): Result<List<UserDto>> {
        return try {
            val response = api.getUsers()
            if (response.success) {
                Result.success(response.users)
            } else {
                Result.failure(Exception("Error al obtener usuarios: success=false"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(name: String, lastName: String, email: String, password: String, role: String = "OPERADOR", departmentId: Int? = null): Result<String> {
        return try {
            // Corrección: Argumentos nombrados
            val body = RegisterRequest(
                name = name, 
                lastName = lastName, 
                email = email, 
                password = password, 
                role = role, 
                departmentId = departmentId
            )
            val response = api.createUser(body)
            if (response.success) {
                 Result.success("Usuario creado correctamente")
            } else {
                 Result.failure(Exception(response.message ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: UserDto): Result<UserDto> {
        return try {
            val response = api.updateUser(user.id, user)
            if (response.success) {
                Result.success(user)
            } else {
                Result.failure(Exception(response.message ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(id: Int): Result<Unit> {
        return try {
            val response = api.deleteUser(id)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
