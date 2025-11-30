package com.example.practica.screen.home

data class SensorUiState(
    val isLoading: Boolean = false,
    val temperature: Float? = null,
    val humidity: Float? = null,
    val lastUpdate: String? = null,
    val errorMessage: String? = null
)