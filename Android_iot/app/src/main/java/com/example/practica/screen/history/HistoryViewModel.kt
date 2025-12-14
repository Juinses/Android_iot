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

    // Si userId es null, carga todo (para Admin). Si tiene valor, filtra (para Operador)
    fun loadEvents(userId: Int? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // La API getAccessEvents ya soporta query parameter userId
                val events = api.getAccessEvents(userId)
                
                // Ordenar por fecha descendente (lo más nuevo primero) si la API no lo hace
                // Asumiendo formato ISO string ordenable o id autoincremental
                val sortedEvents = events.sortedByDescending { it.id } 
                
                _uiState.value = _uiState.value.copy(
                    events = sortedEvents,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar historial: ${e.message}"
                )
            }
        }
    }
}
