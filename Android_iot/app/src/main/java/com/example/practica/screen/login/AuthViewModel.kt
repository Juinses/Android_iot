package com.example.practica.screen.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.AuthRepository
import com.example.practica.data.remote.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Checking : AuthState() // Splash chequeando
    data object Unauthenticated : AuthState() // Ir a Login
    data class Authenticated(val user: UserDto) : AuthState() // Ir a Home
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repo = AuthRepository()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState: StateFlow<AuthState> = _authState

    // Datos temporales para el flujo de recuperación de contraseña
    var tempEmailForReset: String? = null
    var tempCodeForReset: String? = null

    init {
        checkSession()
    }

    private fun appContext() = getApplication<Application>().applicationContext

    fun checkSession() {
        viewModelScope.launch {
            val ctx = appContext()
            // 1) Ver si hay token guardado
            val token = repo.getStoredToken(ctx)
            if (token.isNullOrEmpty()) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }
            // 2) Validar token contra /profile
            val res = repo.validateToken(ctx)
            _authState.value = res.fold(
                onSuccess = { user -> AuthState.Authenticated(user) },
                onFailure = { AuthState.Unauthenticated }
            )
        }
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Checking
        viewModelScope.launch {
            val ctx = appContext()
            val res = repo.login(ctx, email, pass)
            _authState.value = res.fold(
                onSuccess = { AuthState.Authenticated(it.user) },
                onFailure = {
                    AuthState.Error(it.message ?: "Error al iniciar sesión")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Usamos el repo en lugar de TokenStorage directo para limpiar advertencia
            repo.clearToken(appContext())
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun register(name: String, lastName: String, email: String, pass: String, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            val res = repo.register(name, lastName, email, pass)
            res.fold(
                onSuccess = { onSuccess() },
                onFailure = { onFail(it.message ?: "Error al registrar") }
            )
        }
    }

    fun forgotPassword(email: String, onSuccess: (String) -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            val res = repo.forgotPassword(email)
            res.fold(
                onSuccess = { msg -> 
                    tempEmailForReset = email // Guardamos el email exitoso
                    onSuccess(msg) 
                },
                onFailure = { err -> onFail(err.message ?: "Error desconocido") }
            )
        }
    }

    fun resetPassword(email: String, code: String, newPass: String, onSuccess: (String) -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            val res = repo.resetPassword(email, code, newPass)
            res.fold(
                onSuccess = { msg -> 
                    // Limpiamos datos temporales
                    tempEmailForReset = null
                    tempCodeForReset = null
                    onSuccess(msg) 
                },
                onFailure = { err -> onFail(err.message ?: "Error desconocido") }
            )
        }
    }
}