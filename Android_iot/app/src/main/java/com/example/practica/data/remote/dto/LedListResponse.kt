package com.example.practica.data.remote.dto

// Esta clase se usa para mapear la respuesta del backend que tiene la forma { "data": [...] }
data class LedListResponse(
    val data: List<LedDto>
)
