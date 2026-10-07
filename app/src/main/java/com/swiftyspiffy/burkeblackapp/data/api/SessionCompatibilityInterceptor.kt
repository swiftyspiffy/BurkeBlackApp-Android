package com.swiftyspiffy.burkeblackapp.data.api

import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/** Explicit preview protocol; never fall back after a renewal/authentication failure. */
internal class SessionCompatibilityInterceptor(private val enabled: Boolean) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (!enabled || original.url.scheme != "https" || original.url.host != "api.burkeblack.tv" || original.url.port != 443 || !original.url.encodedPath.startsWith("/app/")) return chain.proceed(original)
        // Protocol routing only; the server still authenticates every request.
        val request = original.newBuilder().header("X-Burke-Go-API", "1").build()
        if (!request.header("Authorization").isNullOrEmpty() && request.url.encodedPath != "/app/auth/renew") {
            val renewal = request.newBuilder()
                .url(request.url.newBuilder().encodedPath("/app/auth/renew").query(null).build())
                .post(ByteArray(0).toRequestBody()).removeHeader("Content-Type").build()
            val result = chain.proceed(renewal)
            if (!result.isSuccessful) return result
            result.close()
        }
        val adapted = if (request.url.encodedPath == "/app/twitch-token") {
            request.newBuilder().post(ByteArray(0).toRequestBody()).build()
        } else request
        return chain.proceed(adapted)
    }
}
