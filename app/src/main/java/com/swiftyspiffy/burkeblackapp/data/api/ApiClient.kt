package com.swiftyspiffy.burkeblackapp.data.api

import android.os.Build
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.swiftyspiffy.burkeblackapp.BuildConfig
import kotlinx.serialization.json.Json
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://api.burkeblack.tv/app/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val platformHeaderInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-App-Platform", "Android")
            .addHeader("X-App-Platform-Version", Build.VERSION.RELEASE)
            .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
            .build()
        chain.proceed(request)
    }

    private val appLoggerInterceptor = Interceptor { chain ->
        val request = chain.request()
        val path = request.url.encodedPath.removePrefix("/app")
        AppLogger.log("API: ${request.method} $path")
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(platformHeaderInterceptor)
        .addInterceptor(appLoggerInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: BurkeBlackApi = retrofit.create(BurkeBlackApi::class.java)
}
