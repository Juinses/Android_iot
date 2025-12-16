package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class AccessEventDto(
    @Json(name = "id") val id: Int,
    @Json(name = "fecha_hora") val timestamp: String,
    @Json(name = "usuario_id") val userId: Int, // Corregido: usuario_id
    @Json(name = "usuario_nombre") val userName: String? = null, // Viene del join u.name
    @Json(name = "tipo_evento") val eventType: String,
    @Json(name = "origen") val origin: String,
    @Json(name = "resultado") val result: String,
    @Json(name = "detalle") val detail: String? = null,
    @Json(name = "id_departamento") val departmentId: Int? = null // Podría no venir si la query no lo incluye explícitamente, pero es nullable
)
