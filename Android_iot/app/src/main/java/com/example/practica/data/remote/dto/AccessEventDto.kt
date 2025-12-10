package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class AccessEventDto(
    @Json(name = "id") val id: Int,
    @Json(name = "fecha_hora") val timestamp: String,
    @Json(name = "id_usuario") val userId: Int,
    @Json(name = "nombre_usuario") val userName: String? = null, // Optional for display
    @Json(name = "tipo_evento") val eventType: String, // "ACCESO_PERMITIDO", "ACCESO_DENEGADO", "APERTURA_MANUAL"
    @Json(name = "origen") val origin: String, // "APP", "RFID", "ADMIN"
    @Json(name = "resultado") val result: String // "PERMITIDO", "DENEGADO"
)