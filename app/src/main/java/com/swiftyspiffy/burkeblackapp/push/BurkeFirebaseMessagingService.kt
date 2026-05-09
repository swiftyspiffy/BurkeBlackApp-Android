package com.swiftyspiffy.burkeblackapp.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swiftyspiffy.burkeblackapp.MainActivity
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.util.AppLogger

class BurkeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AppLogger.log("FCM: new token received (${token.take(12)}...)")
        PushNotificationManager.onNewToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        AppLogger.log("FCM: message received type=${message.data["type"]}")

        val title = message.notification?.title ?: message.data["title"] ?: "The Dirty Skull"
        val body = message.notification?.body ?: message.data["body"] ?: message.data["message"] ?: ""
        val url = message.data["url"]
        val type = message.data["type"] ?: "general"

        AppLogger.log("FCM: showing notification type=$type url=${url ?: "none"}")
        showNotification(title, body, url, type)
    }

    private fun showNotification(title: String, body: String, url: String?, type: String) {
        val channelId = getChannelId(type)
        ensureChannel(channelId, getChannelName(type))

        val intent = if (!url.isNullOrBlank()) {
            val parsedUri = Uri.parse(url)
            if (parsedUri.scheme == "burkeblackapp") {
                // Deep link into the app
                Intent(this, MainActivity::class.java).apply {
                    data = parsedUri
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            } else {
                // External URL
                Intent(Intent.ACTION_VIEW, parsedUri)
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
        AppLogger.log("FCM: notification displayed channel=$channelId")
    }

    private fun ensureChannel(id: String, name: String) {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(id) == null) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
            AppLogger.log("FCM: created notification channel id=$id name=$name")
        }
    }

    private fun getChannelId(type: String): String = when (type) {
        "burkeblack_stream", "burke40k_stream" -> "stream_alerts"
        "burke_announcement", "mod_announcement" -> "announcements"
        "special_event" -> "special_events"
        "channel_tidings" -> "tidings"
        "youtube_video" -> "youtube_videos"
        "youtube_short" -> "youtube_shorts"
        "tiktok_video" -> "tiktok_videos"
        "twitter_post" -> "twitter_posts"
        else -> "general"
    }

    private fun getChannelName(type: String): String = when (type) {
        "burkeblack_stream", "burke40k_stream" -> "Stream Alerts"
        "burke_announcement", "mod_announcement" -> "Announcements"
        "special_event" -> "Special Events"
        "channel_tidings" -> "Channel Tidings"
        "youtube_video" -> "YouTube Videos"
        "youtube_short" -> "YouTube Shorts"
        "tiktok_video" -> "TikTok Videos"
        "twitter_post" -> "X Posts"
        else -> "General"
    }
}
