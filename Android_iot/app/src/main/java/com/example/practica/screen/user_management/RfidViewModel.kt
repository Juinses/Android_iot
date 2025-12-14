package com.example.practica.screen.user_management

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.AccessSensorDto
import com.example.practica.data.remote.dto.UserDto
import kotlinx.coroutines.launch

data class RfidUiState(
    val sensors: List<AccessSensorDto> = emptyList(),
    val users: List<UserDto> = emptyList(), // Para el selector de dueño
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class RfidViewModel : ViewModel() {
    private val sensorApi = HttpClient.sensorApi
    private val authApi = HttpClient.authApi // Para listar usuarios

    private val _uiState = mutableStateOf(RfidUiState())
    val uiState: State<RfidUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // Cargamos sensores y usuarios en paralelo (o secuencial simple)
                val sensors = sensorApi.getSensors()
                val usersResponse = authApi.getUsers() 
                val users = usersResponse.users

                _uiState.value = _uiState.value.copy(
                    sensors = sensors,
                    users = users,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando datos: ${e.message}"
                )
            }
        }
    }

    fun createSensor(code: String, type: String, userId: Int, departmentId: Int?) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val newSensor = AccessSensorDto(
                    macAddress = code,
                    type = type,
                    userId = userId,
                    status = "ACTIVO",
                    departmentId = departmentId
                )
                sensorApi.createSensor(newSensor)
                
                // Recargar lista
                loadData()
                _uiState.value = _uiState.value.copy(successMessage = "Sensor registrado correctamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al crear: ${e.message}"
                )
            }
        }
    }

    fun toggleSensorStatus(sensor: AccessSensorDto) {
        viewModelScope.launch {
            try {
                val newStatus = if (sensor.status == "ACTIVO") "BLOQUEADO" else "ACTIVO"
                val updated = sensor.copy(status = newStatus)
                
                // Asumiendo que el ID no es nulo para updates
                sensor.id?.let { id ->
                    sensorApi.updateSensor(id, updated)
                    
                    // Actualizar localmente para feedback rápido
                    val currentList = _uiState.value.sensors.toMutableList()
                    val index = currentList.indexOfFirst { it.id == id }
                    if (index != -1) {
                        currentList[index] = updated
                        _uiState.value = _uiState.value.copy(sensors = currentList)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "No se pudo actualizar estado")
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
