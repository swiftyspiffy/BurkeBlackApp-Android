package com.swiftyspiffy.burkeblackapp.widget

import android.content.Context
import android.content.SharedPreferences

object WidgetDataStore {
    private const val PREFS_NAME = "widget_data"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Crew stats
    fun setCrewStats(context: Context, doubloons: Int, soundbyteCredits: Int, followMonths: Int, subMonths: Int) {
        prefs(context).edit()
            .putInt("doubloons", doubloons)
            .putInt("soundbyteCredits", soundbyteCredits)
            .putInt("followMonths", followMonths)
            .putInt("subMonths", subMonths)
            .putBoolean("isLoggedIn", true)
            .apply()
    }

    fun getDoubloons(context: Context): Int = prefs(context).getInt("doubloons", 0)
    fun getSoundbyteCredits(context: Context): Int = prefs(context).getInt("soundbyteCredits", 0)
    fun getFollowMonths(context: Context): Int = prefs(context).getInt("followMonths", 0)
    fun getSubMonths(context: Context): Int = prefs(context).getInt("subMonths", 0)
    fun isLoggedIn(context: Context): Boolean = prefs(context).getBoolean("isLoggedIn", false)

    fun clearCrewStats(context: Context) {
        prefs(context).edit()
            .remove("doubloons")
            .remove("soundbyteCredits")
            .remove("followMonths")
            .remove("subMonths")
            .putBoolean("isLoggedIn", false)
            .apply()
    }

    // Stream status
    fun setStreamStatus(
        context: Context,
        isLive: Boolean,
        title: String?,
        gameName: String?,
        viewerCount: Int?,
        boxArtUrl: String?,
        startedAt: String?
    ) {
        prefs(context).edit()
            .putBoolean("stream.isLive", isLive)
            .putString("stream.title", title)
            .putString("stream.gameName", gameName)
            .putInt("stream.viewerCount", viewerCount ?: 0)
            .putString("stream.boxArtUrl", boxArtUrl)
            .putString("stream.startedAt", startedAt)
            .apply()
    }

    fun isStreamLive(context: Context): Boolean = prefs(context).getBoolean("stream.isLive", false)
    fun getStreamTitle(context: Context): String? = prefs(context).getString("stream.title", null)
    fun getStreamGameName(context: Context): String? = prefs(context).getString("stream.gameName", null)
    fun getStreamViewerCount(context: Context): Int = prefs(context).getInt("stream.viewerCount", 0)
    fun getStreamBoxArtUrl(context: Context): String? = prefs(context).getString("stream.boxArtUrl", null)
    fun getStreamStartedAt(context: Context): String? = prefs(context).getString("stream.startedAt", null)
}
