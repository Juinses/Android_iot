package com.example.practica.data.remote

import com.example.practica.data.remote.dto.AccessEventDto
import com.example.practica.data.remote.dto.AccessSensorDto
import com.example.practica.data.remote.dto.SensorDataDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SensorApi {
    @GET("iot/data")
    suspend fun getSensorData(): SensorDataDto

    // Access Control Endpoints

    // --- Sensors (Keys/Cards) ---
    @GET("iot/sensors")
    suspend fun getSensors(@Query("departmentId") departmentId: Int? = null): List<AccessSensorDto>

    @POST("iot/sensors")
    suspend fun createSensor(@Body sensor: AccessSensorDto): AccessSensorDto

    @PUT("iot/sensors/{id}")
    suspend fun updateSensor(@Path("id") id: Int, @Body sensor: AccessSensorDto): AccessSensorDto

    // --- Access Events ---
    @GET("iot/events")
    suspend fun getAccessEvents(@Query("userId") userId: Int? = null): List<AccessEventDto>

    @POST("iot/events")
    suspend fun registerAccessEvent(@Body event: AccessEventDto): AccessEventDto
    
    // --- Commands (Manual Open/Close) ---
    @POST("iot/barrier/open")
    suspend fun openBarrier(@Query("userId") userId: Int)

    @POST("iot/barrier/close")
    suspend fun closeBarrier(@Query("userId") userId: Int)
}