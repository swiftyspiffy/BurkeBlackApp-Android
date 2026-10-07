package com.swiftyspiffy.burkeblackapp.data.api

import android.os.Build
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.swiftyspiffy.burkeblackapp.BuildConfig
import kotlinx.serialization.json.Json
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
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

    // Body-level HTTP logging exposes session grants and GIF keys even in debug builds.
    // Keep the existing method/path logger, which excludes headers, query and body.
    private val sessionRenewalInterceptor = Interceptor { chain ->
        val request = chain.request()
        val authenticated = !request.header("Authorization").isNullOrEmpty()
        if (BuildConfig.GO_API_AUTH && authenticated && request.url.host == "api.burkeblack.tv" && request.url.encodedPath != "/app/auth/renew") {
            val renewal = request.newBuilder()
                .url(request.url.newBuilder().encodedPath("/app/auth/renew").query(null).build())
                .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                .removeHeader("Content-Type").build()
            val result = chain.proceed(renewal)
            if (!result.isSuccessful) return@Interceptor result
            result.close()
        }
        // Older release builds retain GET refresh until exact-method routing is ready.
        val adapted = if (BuildConfig.GO_API_AUTH && request.url.encodedPath == "/app/twitch-token") {
            request.newBuilder().post(okhttp3.RequestBody.create(null, ByteArray(0))).build()
        } else request
        chain.proceed(adapted)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(platformHeaderInterceptor)
        .addInterceptor(appLoggerInterceptor)
        .addInterceptor(sessionRenewalInterceptor)
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
