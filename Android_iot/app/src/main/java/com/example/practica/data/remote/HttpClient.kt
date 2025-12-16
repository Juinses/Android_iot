package com.example.practica.data.remote

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object HttpClient {
    private const val BASE_URL = "http://ec2-52-71-213-89.compute-1.amazonaws.com/"
    
    private var retrofit: Retrofit? = null

    fun init(context: Context) {
        if (retrofit != null) return // Ya inicializado

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(AuthInterceptor(context.applicationContext)) // Usamos el contexto para el token
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // Propiedades delegadas que lanzarán excepción si no se ha llamado a init()
    private val currentRetrofit: Retrofit
        get() = retrofit ?: throw IllegalStateException("HttpClient must be initialized with HttpClient.init(context)!")

    val authApi: AuthApi by lazy { currentRetrofit.create(AuthApi::class.java) }
    val sensorApi: SensorApi by lazy { currentRetrofit.create(SensorApi::class.java) }
    val ledApi: LedApi by lazy { currentRetrofit.create(LedApi::class.java) }
    val eventApi: EventApi by lazy { currentRetrofit.create(EventApi::class.java) }
    val accessApi: AccessApi by lazy { currentRetrofit.create(AccessApi::class.java) }
    val barrierApi: BarrierApi by lazy { currentRetrofit.create(BarrierApi::class.java) }
    val departmentApi: DepartmentApi by lazy { currentRetrofit.create(DepartmentApi::class.java) }
}
