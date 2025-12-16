package com.example.practica.data.remote

import com.example.practica.data.remote.dto.AccessValidationRequest
import com.example.practica.data.remote.dto.AccessValidationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AccessApi {
    @POST("api/accesos/validar")
    suspend fun validateAccess(@Body request: AccessValidationRequest): AccessValidationResponse
}
