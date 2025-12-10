package com.example.practica.data

import com.example.practica.data.remote.HttpClient
import com.example.practica.data.remote.dto.AccessEventDto
import com.example.practica.data.remote.dto.AccessSensorDto
import com.example.practica.data.remote.dto.SensorDataDto

class SensorRepository {
    private val api = HttpClient.sensorApi

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
            val sensors = api.getSensors(departmentId)
            Result.success(sensors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSensor(sensor: AccessSensorDto): Result<AccessSensorDto> {
        return try {
            val createdSensor = api.createSensor(sensor)
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

    suspend fun getAccessEvents(userId: Int? = null): Result<List<AccessEventDto>> {
        return try {
            val events = api.getAccessEvents(userId)
            Result.success(events)
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

    suspend fun openBarrier(userId: Int): Result<Unit> {
        return try {
            api.openBarrier(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeBarrier(userId: Int): Result<Unit> {
        return try {
            api.closeBarrier(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}