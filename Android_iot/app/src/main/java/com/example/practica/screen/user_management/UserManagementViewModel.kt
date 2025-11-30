package com.example.practica.screen.user_management

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.AuthRepository
import com.example.practica.data.remote.dto.UserDto
import kotlinx.coroutines.launch

data class UserManagementState(
    val users: List<UserDto> = emptyList(),
    val filteredUsers: List<UserDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class UserManagementViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState = mutableStateOf(UserManagementState())
        private set

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getUsers()
            uiState.value = result.fold(
                onSuccess = { list ->
                    UserManagementState(
                        users = list,
                        filteredUsers = list,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar usuarios: ${e.message}"
                    )
                }
            )
        }
    }

    fun filterUsers(query: String) {
        val all = uiState.value.users
        if (query.isBlank()) {
            uiState.value = uiState.value.copy(filteredUsers = all)
        } else {
            val filtered = all.filter {
                it.name.contains(query, ignoreCase = true) ||
                (it.lastName?.contains(query, ignoreCase = true) == true) ||
                it.email.contains(query, ignoreCase = true)
            }
            uiState.value = uiState.value.copy(filteredUsers = filtered)
        }
    }

    fun createUser(name: String, lastName: String, email: String, pass: String) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.createUser(name, lastName, email, pass)
            result.fold(
                onSuccess = {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        successMessage = "Usuario creado correctamente"
                    )
                    loadUsers() // Recargar lista
                },
                onFailure = {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al crear: ${it.message}"
                    )
                }
            )
        }
    }

    fun updateUser(user: UserDto) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.updateUser(user)
            result.fold(
                onSuccess = {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        successMessage = "Usuario actualizado"
                    )
                    loadUsers()
                },
                onFailure = {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al actualizar: ${it.message}"
                    )
                }
            )
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = repository.deleteUser(id)
            result.fold(
                onSuccess = {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        successMessage = "Usuario eliminado"
                    )
                    loadUsers()
                },
                onFailure = {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al eliminar: ${it.message}"
                    )
                }
            )
        }
    }
    
    fun clearMessages() {
        uiState.value = uiState.value.copy(errorMessage = null, successMessage = null)
    }
}