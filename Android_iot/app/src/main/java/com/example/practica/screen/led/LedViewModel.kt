package com.example.practica.screen.led

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.LedRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LedViewModel : ViewModel() {

    private val repository = LedRepository(HttpClient.ledApi)

    private val _uiState = MutableStateFlow(LedUiState(isLoading = true))
    val uiState: StateFlow<LedUiState> = _uiState

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                loadLeds()
                delay(2000) // Refresh every 2 seconds
            }
        }
    }

    private suspend fun loadLeds() {
        try {
            _uiState.value = _uiState.value.copy(error = null)
            val leds = repository.getLeds()
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                leds = leds
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Error al cargar LEDs"
            )
        }
    }

    fun onToggleLed(id: Int, newState: Boolean) {
        viewModelScope.launch {
            // Optimistic update
            val currentLeds = _uiState.value.leds
            val updatedLeds = currentLeds.map { 
                if (it.id == id) it.copy(state = newState) else it 
            }
            
            _uiState.value = _uiState.value.copy(leds = updatedLeds)

            try {
                repository.updateLed(id, newState)
            } catch (e: Exception) {
                // Revert on failure
                _uiState.value = _uiState.value.copy(
                    leds = currentLeds,
                    error = e.message ?: "Error al actualizar LED $id"
                )
            }
        }
    }
}
