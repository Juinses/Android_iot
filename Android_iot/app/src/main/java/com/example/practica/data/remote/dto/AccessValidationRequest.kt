package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class AccessValidationRequest(
    @Json(name = "codigo_sensor") val sensorCode: String
)
