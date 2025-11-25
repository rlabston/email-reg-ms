package com.android.ai.catalog.network

import com.android.ai.catalog.BuildConfig
import com.android.ai.catalog.auth.AuthManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val authInterceptor = Interceptor { chain ->
        val original: Request = chain.request()
        val token = AuthManager.getToken()
        val builder = original.newBuilder()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        chain.proceed(builder.build())
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(selectBaseUrl())
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private fun selectBaseUrl(): String {
        // Use emulator URL when running on emulator; fallback to device URL.
        // Simple heuristic: 10.0.2.2 reachable only from emulator environment.
        return BuildConfig.EMULATOR_SERVER_URL.ifBlank { BuildConfig.DEVICE_SERVER_URL }
    }

    val api: ApiService = retrofit.create(ApiService::class.java)
}
