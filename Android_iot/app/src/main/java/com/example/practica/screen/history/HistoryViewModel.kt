package com.example.practica.screen.history

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.AccessEventDto
import kotlinx.coroutines.launch

data class HistoryUiState(
    val events: List<AccessEventDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HistoryViewModel : ViewModel() {
    private val api = HttpClient.sensorApi
    private val _uiState = mutableStateOf(HistoryUiState())
    val uiState: State<HistoryUiState> = _uiState

    // Ahora requiere departmentId obligatorio para saber qué lista pedir
    fun loadEvents(departmentId: Int, userId: Int? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // Pedimos los eventos del departamento real del usuario
                val response = api.getAccessEvents(departmentId)
                val events = response.data
                
                // Ordenar por fecha descendente
                val sortedEvents = events.sortedByDescending { it.id } 
                
                // Filtramos localmente si es necesario (para Operador que solo quiere ver los suyos)
                // Aunque el requerimiento dice "visualizar el historial de accesos del departamento", 
                // así que un operador podría ver los de todos en su depto.
                // Si userId viene (filtro explícito), lo aplicamos.
                val filteredEvents = if (userId != null) {
                    sortedEvents.filter { it.userId == userId }
                } else {
                    sortedEvents
                }
                
                _uiState.value = _uiState.value.copy(
                    events = filteredEvents,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }
}
