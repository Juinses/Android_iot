package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class AccessSensorDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "codigo_sensor") val macAddress: String, // UID or MAC
    @Json(name = "tipo") val type: String, // "Llavero" or "Tarjeta"
    @Json(name = "usuario_id") val userId: Int, // Corregido para coincidir con backend (usuario_id)
    @Json(name = "estado") val status: String, // "ACTIVO", "PERDIDO", "BLOQUEADO"
    @Json(name = "departamento_id") val departmentId: Int? = null // Corregido (departamento_id)
)
