package com.example.practica.data.remote

import com.example.practica.data.remote.dto.LedDto
import com.example.practica.data.remote.dto.LedUpdateRequest

class LedRepository(
    private val api: LedApi
) {

    suspend fun getLeds(): List<LedDto> {
        return api.getLeds()
    }

    suspend fun updateLed(id: Int, state: Boolean): LedDto {
        val body = LedUpdateRequest(state = state)
        return api.updateLed(id, body)
    }
}
