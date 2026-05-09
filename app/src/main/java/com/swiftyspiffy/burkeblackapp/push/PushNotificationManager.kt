package com.swiftyspiffy.burkeblackapp.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object PushNotificationManager {

    private const val PREF_NAME = "push_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel("stream_alerts", "Stream Alerts", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel("announcements", "Announcements", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel("special_events", "Special Events", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel("tidings", "Channel Tidings", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel("twitter_posts", "X Posts", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel("general", "General", NotificationManager.IMPORTANCE_DEFAULT),
        )
        channels.forEach { manager.createNotificationChannel(it) }
        AppLogger.log("Push: notification channels created")
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    suspend fun getFcmToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            AppLogger.log("Push: FCM token retrieved (${token.take(12)}...)")
            token
        } catch (e: Exception) {
            AppLogger.log("Push: FCM token retrieval failed: ${e.message}")
            null
        }
    }

    fun onNewToken(context: Context, token: String) {
        // Save token locally for later registration
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
        AppLogger.log("Push: FCM token saved locally")
    }

    fun getSavedToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)
    }

    suspend fun registerWithBackend(bearerToken: String, fcmToken: String) {
        try {
            val body = JsonObject(mapOf(
                "platform" to JsonPrimitive("android"),
                "device_token" to JsonPrimitive(fcmToken),
            ))
            ApiClient.api.registerDeviceToken("Bearer $bearerToken", body)
            AppLogger.log("Push: device token registered with backend")
        } catch (e: Exception) {
            AppLogger.log("Push: backend registration failed: ${e.message}")
        }
    }

    suspend fun refreshTokenRegistration(context: Context, bearerToken: String?) {
        if (!hasNotificationPermission(context)) {
            AppLogger.log("Push: skipping token refresh — no notification permission")
            return
        }
        if (bearerToken.isNullOrEmpty()) {
            AppLogger.log("Push: skipping token refresh — not logged in")
            return
        }
        val fcmToken = getFcmToken()
        if (fcmToken == null) {
            AppLogger.log("Push: skipping token refresh — FCM token unavailable")
            return
        }
        // Save locally in case it changed
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, fcmToken)
            .apply()
        registerWithBackend(bearerToken, fcmToken)
        AppLogger.log("Push: token refresh completed on app resume")
    }

    suspend fun unregisterFromBackend(bearerToken: String, fcmToken: String) {
        try {
            val body = JsonObject(mapOf(
                "device_token" to JsonPrimitive(fcmToken),
            ))
            ApiClient.api.removeDeviceToken("Bearer $bearerToken", body)
            AppLogger.log("Push: device token unregistered from backend")
        } catch (e: Exception) {
            AppLogger.log("Push: backend unregister failed: ${e.message}")
        }
    }
}
