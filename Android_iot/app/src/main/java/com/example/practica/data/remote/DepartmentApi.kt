package com.example.practica.data.remote

import com.example.practica.data.remote.dto.DepartmentDto
import com.example.practica.data.remote.dto.DepartmentListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DepartmentApi {
    @GET("api/departamentos")
    suspend fun getDepartments(): DepartmentListResponse

    @GET("api/departamentos/{id}")
    suspend fun getDepartment(@Path("id") id: Int): DepartmentDto

    @POST("api/departamentos")
    suspend fun createDepartment(@Body department: DepartmentDto): DepartmentDto
}
