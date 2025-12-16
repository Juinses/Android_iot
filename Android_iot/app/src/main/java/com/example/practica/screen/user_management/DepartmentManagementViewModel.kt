package com.example.practica.screen.user_management

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.DepartmentDto
import kotlinx.coroutines.launch

data class DepartmentUiState(
    val departments: List<DepartmentDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class DepartmentManagementViewModel : ViewModel() {
    private val api = HttpClient.departmentApi
    private val _uiState = mutableStateOf(DepartmentUiState())
    val uiState: State<DepartmentUiState> = _uiState

    init {
        loadDepartments()
    }

    fun loadDepartments() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val response = api.getDepartments()
                _uiState.value = _uiState.value.copy(
                    departments = response.data,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar departamentos: ${e.message}"
                )
            }
        }
    }

    fun createDepartment(number: String, tower: String, otherData: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            try {
                // ID es ignorado en creación, enviamos 0
                val dept = DepartmentDto(id = 0, number = number, tower = tower, otherData = otherData)
                api.createDepartment(dept)
                
                _uiState.value = _uiState.value.copy(
                    successMessage = "Departamento creado correctamente"
                )
                loadDepartments()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al crear: ${e.message}"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
