package com.example.practica.data.remote

import com.example.practica.data.remote.dto.BarrierResponse
import com.example.practica.data.remote.dto.BarrierStatusDto
import retrofit2.http.GET
import retrofit2.http.POST

interface BarrierApi {
    @GET("api/barrera/estado")
    suspend fun getStatus(): BarrierStatusDto

    @POST("api/barrera/abrir")
    suspend fun openBarrier(): BarrierResponse

    @POST("api/barrera/cerrar")
    suspend fun closeBarrier(): BarrierResponse
}
