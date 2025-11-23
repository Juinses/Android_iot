package com.example.practica.screen.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.AuthRepository
import com.example.practica.data.local.TokenStorage
import com.example.practica.data.remote.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState { data object Checking : AuthState() // Splash chequeando
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
            TokenStorage.clearToken(appContext())
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun register(name: String, email: String, pass: String, onSuccess: () -> Unit, onFail:
        (String) -> Unit) {
        viewModelScope.launch {
            val res = repo.register(name, email, pass)
            res.fold(
                onSuccess = { onSuccess() },
                onFailure = { onFail(it.message ?: "Error al registrar") }
            )
        }
    }

}

