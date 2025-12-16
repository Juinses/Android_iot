package com.example.practica.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.AccessEventDto
import com.example.practica.data.remote.dto.AccessValidationRequest
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
    private val sensorApi = HttpClient.sensorApi
    private val barrierApi = HttpClient.barrierApi
    private val accessApi = HttpClient.accessApi // NUEVO: Para validar acceso (Llavero Digital)
    private val connectivityObserver = NetworkConnectivityObserver(application)

    private val _accessState = MutableStateFlow<AccessState>(AccessState.Idle)
    val accessState: StateFlow<AccessState> = _accessState

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val _lastEvent = MutableStateFlow<AccessEventDto?>(null)
    val lastEvent: StateFlow<AccessEventDto?> = _lastEvent

    private val _barrierStatus = MutableStateFlow("CERRADA")
    val barrierStatus: StateFlow<String> = _barrierStatus

    init {
        observeConnectivity()
        startPolling()
    }

    private fun observeConnectivity() {
        connectivityObserver.observe().onEach { status ->
            _isOnline.value = status == NetworkStatus.Available
        }.launchIn(viewModelScope)
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                if (_isOnline.value) {
                    try {
                        val response = sensorApi.getAccessEvents(departmentId = 1, limit = 5)
                        val events = response.data
                        
                        val statusDto = barrierApi.getStatus()
                        _barrierStatus.value = statusDto.estado

                        if (events.isNotEmpty()) {
                            val latest = events.firstOrNull()
                            if (latest != null && latest.id != _lastEvent.value?.id) {
                                _lastEvent.value = latest
                            }
                        }
                    } catch (e: Exception) {
                        // Ignorar
                    }
                }
                delay(3000)
            }
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
        
        if (!isDigitalKey) {
            // ADMIN: Usar BarrierApi (Endpoint oficial de admin)
            openBarrierAdmin()
        } else {
            // OPERADOR: Llavero Digital
            // Usamos AccessApi (/accesos/validar) simulando ser un sensor del usuario
            openBarrierOperator(userId)
        }
    }

    private fun openBarrierAdmin() {
        viewModelScope.launch {
            _accessState.value = AccessState.Loading
            try {
                val response = barrierApi.openBarrier()
                if (response.ok) {
                    _accessState.value = AccessState.Success("Barrera abierta (Admin)")
                    _barrierStatus.value = response.estado
                    VibrationHelper.vibrateSuccess(getApplication())
                } else {
                     _accessState.value = AccessState.Error("Fallo al abrir barrera")
                }
            } catch (e: Exception) {
                _accessState.value = AccessState.Error("Error: ${e.message}")
            }
        }
    }

    private fun openBarrierOperator(userId: Int) {
        viewModelScope.launch {
            _accessState.value = AccessState.Loading
            try {
                // 1. Obtener todos los sensores del usuario
                // Asumimos depto 1 por ahora (TODO: obtener real)
                val sensorsResponse = sensorApi.getSensors(departmentId = 1)
                val userSensors = sensorsResponse.data.filter { it.userId == userId }

                // 2. Intentar buscar uno ACTIVO
                val activeSensor = userSensors.find { it.status == "ACTIVO" }
                
                // 3. Si hay uno ACTIVO, lo usamos para validar y abrir
                if (activeSensor != null) {
                    val request = AccessValidationRequest(sensorCode = activeSensor.macAddress)
                    val response = accessApi.validateAccess(request)

                    if (response.allowed) {
                        _accessState.value = AccessState.Success("Acceso Permitido (Llavero Digital)")
                        _barrierStatus.value = "ABIERTA" // Feedback visual inmediato
                        VibrationHelper.vibrateSuccess(getApplication())
                        
                        // Auto-cerrar visual
                        launch {
                            delay(10000)
                            _barrierStatus.value = "CERRADA"
                        }
                    } else {
                        _accessState.value = AccessState.Error("Acceso Denegado: ${response.reason}")
                        VibrationHelper.vibrateError(getApplication())
                    }
                    return@launch
                }
                
                // 4. Si NO hay sensores activos, buscamos si tiene uno bloqueado/perdido para INTENTAR validar y generar el log de denegación
                //    Esto cumple con el requerimiento de "guardar en historial" incluso si falla
                val blockedSensor = userSensors.firstOrNull() // Tomamos cualquiera, aunque esté bloqueado
                
                if (blockedSensor != null) {
                    // Intentamos validar con el sensor bloqueado. El backend debería responder allowed=false y registrar el evento "DENEGADO"
                    val request = AccessValidationRequest(sensorCode = blockedSensor.macAddress)
                    val response = accessApi.validateAccess(request)
                    
                    // Aquí siempre esperamos que sea false, pero manejamos la respuesta igual
                    if (!response.allowed) {
                         _accessState.value = AccessState.Error("Acceso Denegado: ${response.reason ?: "Sensor no activo"}")
                         VibrationHelper.vibrateError(getApplication())
                    } else {
                        // Caso raro: el backend lo dejó pasar aunque la app pensaba que estaba bloqueado (inconsistencia)
                        _accessState.value = AccessState.Success("Acceso Permitido (Extraño)")
                    }
                } else {
                    // 5. Si no tiene NINGÚN sensor asignado (ni activo ni bloqueado)
                    _accessState.value = AccessState.Error("No tienes ningún sensor asignado (ni activo ni inactivo)")
                }

            } catch (e: Exception) {
                // Si falla (ej: 404 si no hay endpoint, o error de red)
                _accessState.value = AccessState.Error("Error de conexión o backend: ${e.message}")
            }
        }
    }

    fun closeBarrier(userId: Int) {
        if (!_isOnline.value) {
            _accessState.value = AccessState.Error("Sin conexión a Internet")
            return
        }
        viewModelScope.launch {
            _accessState.value = AccessState.Loading
            try {
                val response = barrierApi.closeBarrier()
                if (response.ok) {
                    _accessState.value = AccessState.Success("Barrera cerrada")
                    _barrierStatus.value = response.estado
                    VibrationHelper.vibrateSuccess(getApplication())
                } else {
                     _accessState.value = AccessState.Error("Fallo al cerrar")
                }
            } catch (e: Exception) {
                _accessState.value = AccessState.Error("Error: ${e.message}")
            }
        }
    }
}
