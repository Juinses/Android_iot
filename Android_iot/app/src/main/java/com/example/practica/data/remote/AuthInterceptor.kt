package com.example.practica.data.remote

import android.content.Context
import com.example.practica.data.local.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Obtenemos el token de forma síncrona (bloqueante)
        val token = runBlocking {
            TokenStorage.getToken(context)
        }

        // Creamos la nueva petición con el header
        val newRequest = chain.request().newBuilder()
        if (token != null) {
            newRequest.addHeader("Authorization", "Bearer $token")
        }
        
        return chain.proceed(newRequest.build())
    }
}
