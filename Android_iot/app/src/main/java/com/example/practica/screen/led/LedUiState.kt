package com.example.practica.screen.led

import com.example.practica.data.remote.dto.LedDto

data class LedUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val leds: List<LedDto> = emptyList()
)
