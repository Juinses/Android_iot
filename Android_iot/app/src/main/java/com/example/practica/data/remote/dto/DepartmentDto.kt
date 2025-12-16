package com.example.practica.data.remote.dto

import com.squareup.moshi.Json

data class DepartmentDto(
    @Json(name = "id") val id: Int,
    @Json(name = "numero") val number: String,
    @Json(name = "torre") val tower: String,
    @Json(name = "otros_datos") val otherData: String? = null
)
