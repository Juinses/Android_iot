package com.example.practica.data

import com.example.practica.data.remote.HttpClient
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
}