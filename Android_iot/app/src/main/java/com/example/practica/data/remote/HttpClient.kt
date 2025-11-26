package com.example.practica.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object HttpClient {
    // OJO: si tu backend escucha en un puerto, agrégalo (ej: :3000)
    private const val BASE_URL = "http://ec2-52-71-213-89.compute-1.amazonaws.com/"
    // Moshi con soporte para data classes de Kotlin
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
}