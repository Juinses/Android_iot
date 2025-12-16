package com.example.practica.data.remote

import com.example.practica.data.remote.dto.AccessSensorDto
import com.example.practica.data.remote.dto.EventListResponse
import com.example.practica.data.remote.dto.SensorDataDto
import com.example.practica.data.remote.dto.SensorListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SensorApi {
    // --- Telemetría (IoT) ---
    // Esta ruta sigue bajo /iot/data según app.js
    @GET("iot/data")
    suspend fun getSensorData(): SensorDataDto

    // --- Control de Acceso: Sensores (RFID/Llaveros) ---
    // Backend: GET /api/departamentos/:id/sensores -> { data: [...] }
    @GET("api/departamentos/{deptId}/sensores")
    suspend fun getSensors(@Path("deptId") departmentId: Int): SensorListResponse

    // Backend: POST /api/departamentos/:id/sensores -> AccessSensorDto
    @POST("api/departamentos/{deptId}/sensores")
    suspend fun createSensor(
        @Path("deptId") departmentId: Int,
        @Body sensor: AccessSensorDto
    ): AccessSensorDto

    // Backend: PATCH /api/sensores/:id -> AccessSensorDto
    // Usamos PATCH según sensores.js
    @PATCH("api/sensores/{id}")
    suspend fun updateSensor(@Path("id") id: Int, @Body sensor: AccessSensorDto): AccessSensorDto

    // --- Control de Acceso: Historial ---
    // Backend: GET /api/departamentos/:id/eventos -> { data: [...] }
    @GET("api/departamentos/{deptId}/eventos")
    suspend fun getAccessEvents(
        @Path("deptId") departmentId: Int,
        @Query("limit") limit: Int = 20
    ): EventListResponse

    // Registro manual de eventos (si fuera necesario desde la app)
    @POST("api/eventos")
    suspend fun registerAccessEvent(@Body event: com.example.practica.data.remote.dto.AccessEventDto): com.example.practica.data.remote.dto.AccessEventDto
    
    // --- Comandos Barrera ---
    // Rutas tentativas según app.use('/api', barreraRoutes)
    @POST("api/barrera/abrir")
    suspend fun openBarrier(@Query("userId") userId: Int)

    @POST("api/barrera/cerrar")
    suspend fun closeBarrier(@Query("userId") userId: Int)
}
