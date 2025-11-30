package com.example.practica.screen.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.SensorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SensorViewModel(
    private val repository: SensorRepository = SensorRepository()
) : ViewModel() {

    var uiState = mutableStateOf(SensorUiState())
        private set

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {   // mientras el ViewModel esté vivo
                loadSensorData()
                delay(2000L)    // 2 segundos
            }
        }
    }

    private suspend fun loadSensorData() {
        // OJO: No ponemos isLoading = true a cada rato para evitar parpadeos molestos
        // Solo si quisiéramos mostrar un spinner cada 2 segundos.
        // uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)

        val result = repository.getSensorData()

        uiState.value = result.fold(
            onSuccess = { dto ->
                uiState.value.copy(
                    isLoading = false,
                    temperature = dto.temperature,
                    humidity = dto.humidity,
                    lastUpdate = dto.timestamp,
                    errorMessage = null
                )
            },
            onFailure = { e ->
                uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al obtener datos: ${e.message}"
                )
            }
        )
    }
}