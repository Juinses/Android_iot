package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class EventRequestDto(
    @Json(name = "tipo_evento") val eventType: String, // "APERTURA_MANUAL", "CIERRE_MANUAL"
    @Json(name = "origen") val origin: String = "APP",
    @Json(name = "id_usuario") val userId: Int
)

data class EventResponseDto(
    @Json(name = "id_evento") val eventId: Int,
    @Json(name = "resultado") val result: String, // "PERMITIDO", "DENEGADO"
    val message: String? = null
)
