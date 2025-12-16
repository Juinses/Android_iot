package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class AccessValidationResponse(
    @Json(name = "permitido") val allowed: Boolean,
    @Json(name = "motivo") val reason: String? = null,
    @Json(name = "accion") val action: String? = null,
    @Json(name = "tiempo_cierre_auto") val autoCloseTime: Int? = null
)
