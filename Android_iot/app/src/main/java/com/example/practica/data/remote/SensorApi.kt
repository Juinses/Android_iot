package com.example.practica.data.remote

import com.example.practica.data.remote.dto.SensorDataDto
import retrofit2.http.GET

interface SensorApi {
    @GET("iot/data")
    suspend fun getSensorData(): SensorDataDto
}