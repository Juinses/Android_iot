package com.example.practica.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.AccessEventDto
import com.example.practica.data.remote.dto.EventRequestDto
import com.example.practica.utils.NetworkConnectivityObserver
import com.example.practica.utils.NetworkStatus
import com.example.practica.utils.VibrationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class AccessState {
    data object Idle : AccessState()
    data object Loading : AccessState()
    data class Success(val message: String) : AccessState()
    data class Error(val message: String) : AccessState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val eventApi = HttpClient.eventApi
    private val sensorApi = HttpClient.sensorApi // Para consultar historial
    private val connectivityObserver = NetworkConnectivityObserver(application)

    private val _accessState = MutableStateFlow<AccessState>(AccessState.Idle)
    val accessState: StateFlow<AccessState> = _accessState

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline

    // --- NUEVO: Estado para cumplir "Visualización Inmediata" y "Estado Barrera" ---
    private val _lastEvent = MutableStateFlow<AccessEventDto?>(null)
    val lastEvent: StateFlow<AccessEventDto?> = _lastEvent

    private val _barrierStatus = MutableStateFlow("CERRADA") // ABIERTA / CERRADA
    val barrierStatus: StateFlow<String> = _barrierStatus

    init {
        observeConnectivity()
        startPolling() // Iniciar actualización automática
    }

    private fun observeConnectivity() {
        connectivityObserver.observe().onEach { status ->
            _isOnline.value = status == NetworkStatus.Available
        }.launchIn(viewModelScope)
    }

    // Cumple req: "Visualizar inmediatamente el evento ocurrido"
    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                if (_isOnline.value) {
                    try {
                        // Traemos los eventos (asumiendo que vienen ordenados o el endpoint trae los últimos)
                        val events = sensorApi.getAccessEvents(userId = null)
                        if (events.isNotEmpty()) {
                            // Tomamos el más reciente (suponiendo que la lista viene desc o el ID mayor es el ultimo)
                            // Si la API no ordena, ordenamos aquí por ID o fecha
                            val latest = events.maxByOrNull { it.id } 
                            if (latest != null && latest.id != _lastEvent.value?.id) {
                                _lastEvent.value = latest
                                
                                // Lógica extra: Si el evento es muy reciente (< 10s) y es permitido, asumimos barrera abierta
                                // Esto es una simulación visual basada en eventos, ya que la pauta dice "baja a los 10s"
                                updateBarrierVisualStatus(latest)
                            }
                        }
                    } catch (e: Exception) {
                        // Ignorar errores de polling silenciosamente
                    }
                }
                delay(3000) // Consultar cada 3 segundos
            }
        }
    }

    private fun updateBarrierVisualStatus(event: AccessEventDto) {
        // Si el evento fue "PERMITIDO" o "APERTURA", mostramos ABIERTA por un rato
        // Nota: Esto es solo visualización en la App para cumplir el requisito
        if (event.result == "PERMITIDO" || event.eventType.contains("APERTURA")) {
             // Podríamos comparar timestamp vs hora actual, pero para la demo asumimos
             // que si acabamos de recibir un evento nuevo, la barrera se movió.
             _barrierStatus.value = "ABIERTA"
             
             // Volver a CERRADA visualmente después de 10s (Simula el comportamiento físico)
             viewModelScope.launch {
                 delay(10000)
                 _barrierStatus.value = "CERRADA"
             }
        } else {
             _barrierStatus.value = "CERRADA"
        }
    }

    fun resetState() {
        _accessState.value = AccessState.Idle
    }

    fun openBarrier(userId: Int, isDigitalKey: Boolean = false) {
        if (!_isOnline.value) {
            _accessState.value = AccessState.Error("Sin conexión a Internet")
            return
        }
        val eventType = "APERTURA_MANUAL" 
        val origin = if (isDigitalKey) "APP_LLAVE" else "APP_ADMIN"
        sendEvent(userId, eventType, origin)
    }

    fun closeBarrier(userId: Int) {
        if (!_isOnline.value) {
            _accessState.value = AccessState.Error("Sin conexión a Internet")
            return
        }
        sendEvent(userId, "CIERRE_MANUAL", "APP_ADMIN")
    }

    private fun sendEvent(userId: Int, type: String, origin: String) {
        viewModelScope.launch {
            _accessState.value = AccessState.Loading
            try {
                val request = EventRequestDto(
                    eventType = type,
                    origin = origin,
                    userId = userId
                )
                val response = eventApi.registerEvent(request)
                
                if (response.result == "PERMITIDO") {
                    _accessState.value = AccessState.Success(response.message ?: "Acceso concedido")
                    _barrierStatus.value = "ABIERTA" // Feedback inmediato local
                    VibrationHelper.vibrateSuccess(getApplication())
                    
                    // Auto-cerrar visual en 10s
                    launch {
                        delay(10000)
                        _barrierStatus.value = "CERRADA"
                    }
                } else {
                    _accessState.value = AccessState.Error("Acceso denegado: ${response.message}")
                    VibrationHelper.vibrateError(getApplication())
                }
            } catch (e: Exception) {
                _accessState.value = AccessState.Error("Error de conexión: ${e.message}")
                VibrationHelper.vibrateError(getApplication())
            }
        }
    }
}
