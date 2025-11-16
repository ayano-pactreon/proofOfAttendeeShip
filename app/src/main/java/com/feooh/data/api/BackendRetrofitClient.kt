package com.feooh.data.api

import com.feooh.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton for Backend API
 */
object BackendRetrofitClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Creates Retrofit instance with backend API URL from BuildConfig
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Gets the backend API service instance
     * @param baseUrl Optional base URL override, defaults to BuildConfig.BACKEND_API_URL
     */
    fun getBackendApiService(baseUrl: String = BuildConfig.BACKEND_API_URL): BackendApiService {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return createRetrofit(url).create(BackendApiService::class.java)
    }
}