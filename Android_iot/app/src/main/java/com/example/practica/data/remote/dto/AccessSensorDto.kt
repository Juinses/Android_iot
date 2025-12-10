package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class AccessSensorDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "codigo_sensor") val macAddress: String, // UID or MAC
    @Json(name = "tipo") val type: String, // "Llavero" or "Tarjeta"
    @Json(name = "id_usuario") val userId: Int,
    @Json(name = "estado") val status: String, // "ACTIVO", "PERDIDO", "BLOQUEADO"
    @Json(name = "id_departamento") val departmentId: Int? = null
)