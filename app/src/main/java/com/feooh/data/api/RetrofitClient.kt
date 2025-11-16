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
 * Retrofit client singleton for Luma API and Merchandise API
 */
object RetrofitClient {

    private const val LUMA_BASE_URL = "https://public-api.luma.com/"

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

    // Retrofit instance for Luma API
    private val lumaRetrofit = Retrofit.Builder()
        .baseUrl(LUMA_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val lumaApiService: LumaApiService = lumaRetrofit.create(LumaApiService::class.java)

    // Retrofit instance for Merchandise API (backend)
    private val merchandiseRetrofit by lazy {
        val baseUrl = BuildConfig.BACKEND_API_URL.ifEmpty { "http://localhost:8000" }
        // Ensure base URL ends with /
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val merchandiseApiService: MerchandiseApiService by lazy {
        merchandiseRetrofit.create(MerchandiseApiService::class.java)
    }
}
