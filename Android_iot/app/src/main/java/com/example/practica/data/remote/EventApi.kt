package com.example.practica.data.remote

import com.example.practica.data.remote.dto.EventRequestDto
import com.example.practica.data.remote.dto.EventResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface EventApi {
    @POST("api/eventos") // Ajustar según tu backend real
    suspend fun registerEvent(@Body request: EventRequestDto): EventResponseDto
}
