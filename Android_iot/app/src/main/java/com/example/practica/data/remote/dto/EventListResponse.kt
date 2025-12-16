package com.example.practica.data.remote.dto

data class EventListResponse(
    val data: List<AccessEventDto>,
    val total: Int
)
