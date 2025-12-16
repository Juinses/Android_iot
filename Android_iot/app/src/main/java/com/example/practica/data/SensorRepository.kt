package com.example.practica.data

import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.AccessEventDto
import com.example.practica.data.remote.dto.AccessSensorDto
import com.example.practica.data.remote.dto.SensorDataDto

class SensorRepository {
    private val api = HttpClient.sensorApi
    // Si necesitas usar BarrierApi directamente aquí, puedes descomentar:
    private val barrierApi = HttpClient.barrierApi

    suspend fun getSensorData(): Result<SensorDataDto> {
        return try {
            val data = api.getSensorData()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Access Control Methods ---

    suspend fun getSensors(departmentId: Int? = null): Result<List<AccessSensorDto>> {
        return try {
            // Se requiere departmentId. Usamos 1 por defecto si es nulo.
            val targetDeptId = departmentId ?: 1
            val response = api.getSensors(targetDeptId)
            Result.success(response.data) // Desempaquetamos SensorListResponse
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSensor(sensor: AccessSensorDto): Result<AccessSensorDto> {
        return try {
            // Se requiere departmentId en la URL. Usamos sensor.departmentId o 1.
            val targetDeptId = sensor.departmentId ?: 1
            val createdSensor = api.createSensor(targetDeptId, sensor)
            Result.success(createdSensor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSensor(id: Int, sensor: AccessSensorDto): Result<AccessSensorDto> {
        return try {
            val updatedSensor = api.updateSensor(id, sensor)
            Result.success(updatedSensor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAccessEvents(userId: Int? = null, departmentId: Int? = null): Result<List<AccessEventDto>> {
        return try {
            // Usamos departmentId o 1 por defecto.
            val targetDeptId = departmentId ?: 1
            val response = api.getAccessEvents(targetDeptId)
            val events = response.data
            
            // Filtramos por userId si se proporciona (el backend filtra por departamento)
            val filteredEvents = if (userId != null) {
                events.filter { it.userId == userId }
            } else {
                events
            }
            Result.success(filteredEvents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerAccessEvent(event: AccessEventDto): Result<AccessEventDto> {
        return try {
            val registeredEvent = api.registerAccessEvent(event)
            Result.success(registeredEvent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Estos métodos usaban SensorApi.openBarrier que ya no existe o cambió.
    // Usamos BarrierApi en su lugar.
    suspend fun openBarrier(userId: Int): Result<Unit> {
        return try {
            val response = barrierApi.openBarrier()
            if (response.ok) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Fallo al abrir barrera"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeBarrier(userId: Int): Result<Unit> {
        return try {
            val response = barrierApi.closeBarrier()
            if (response.ok) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Fallo al cerrar barrera"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
