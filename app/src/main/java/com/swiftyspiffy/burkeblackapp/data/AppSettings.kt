package com.swiftyspiffy.burkeblackapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.TimeZone

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppSettings(private val context: Context) {

    // Notification toggles
    private val burkeStreamEnabled = booleanPreferencesKey("settings.notif.burkeStream.enabled")
    private val burkeStreamAllDay = booleanPreferencesKey("settings.notif.burkeStream.allDay")
    private val burkeStreamDays = stringPreferencesKey("settings.notif.burkeStream.days")
    private val burkeStreamFromHour = intPreferencesKey("settings.notif.burkeStream.fromHour")
    private val burkeStreamFromMinute = intPreferencesKey("settings.notif.burkeStream.fromMinute")
    private val burkeStreamToHour = intPreferencesKey("settings.notif.burkeStream.toHour")
    private val burkeStreamToMinute = intPreferencesKey("settings.notif.burkeStream.toMinute")

    private val burke40kEnabled = booleanPreferencesKey("settings.notif.burke40k.enabled")
    private val burke40kAllDay = booleanPreferencesKey("settings.notif.burke40k.allDay")
    private val burke40kDays = stringPreferencesKey("settings.notif.burke40k.days")
    private val burke40kFromHour = intPreferencesKey("settings.notif.burke40k.fromHour")
    private val burke40kFromMinute = intPreferencesKey("settings.notif.burke40k.fromMinute")
    private val burke40kToHour = intPreferencesKey("settings.notif.burke40k.toHour")
    private val burke40kToMinute = intPreferencesKey("settings.notif.burke40k.toMinute")

    private val burkeAnnouncementsEnabled = booleanPreferencesKey("settings.notif.burkeAnnouncements")
    private val modAnnouncementsEnabled = booleanPreferencesKey("settings.notif.modAnnouncements")
    private val specialEventsEnabled = booleanPreferencesKey("settings.notif.specialEvents")
    private val tidingsEnabled = booleanPreferencesKey("settings.notif.tidings")
    private val youtubeVideosEnabled = booleanPreferencesKey("settings.notif.youtubeVideos")
    private val youtubeShortsEnabled = booleanPreferencesKey("settings.notif.youtubeShorts")
    private val tiktokVideosEnabled = booleanPreferencesKey("settings.notif.tiktokVideos")
    private val twitterPostsEnabled = booleanPreferencesKey("settings.notif.twitterPosts")

    // Other settings
    private val giveawayPopupsEnabled = booleanPreferencesKey("settings.giveaways.popupsEnabled")
    private val presentationMode = booleanPreferencesKey("settings.ui.presentationMode")
    private val showCaptainsDispatch = booleanPreferencesKey("settings.debug.showCaptainsDispatch")
    private val pirateThemeEnabled = booleanPreferencesKey("settings.ui.pirateTheme")
    private val pushHasAskedPermission = booleanPreferencesKey("push_has_asked_permission")
    private val debugOverrideInteractionsDisabled = booleanPreferencesKey("settings.debug.overrideInteractionsDisabled")
    private val debugUseTestOverlay = booleanPreferencesKey("settings.debug.useTestOverlay")

    // --- Flow accessors ---

    val burkeStreamEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[burkeStreamEnabled] ?: true }
    val burkeStreamAllDayFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[burkeStreamAllDay] ?: true }
    val burkeStreamDaysFlow: Flow<String> = context.settingsDataStore.data.map { it[burkeStreamDays] ?: "0,1,2,3,4,5,6" }
    val burkeStreamFromHourFlow: Flow<Int> = context.settingsDataStore.data.map { it[burkeStreamFromHour] ?: 9 }
    val burkeStreamFromMinuteFlow: Flow<Int> = context.settingsDataStore.data.map { it[burkeStreamFromMinute] ?: 0 }
    val burkeStreamToHourFlow: Flow<Int> = context.settingsDataStore.data.map { it[burkeStreamToHour] ?: 23 }
    val burkeStreamToMinuteFlow: Flow<Int> = context.settingsDataStore.data.map { it[burkeStreamToMinute] ?: 0 }

    val burke40kEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[burke40kEnabled] ?: true }
    val burke40kAllDayFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[burke40kAllDay] ?: true }
    val burke40kDaysFlow: Flow<String> = context.settingsDataStore.data.map { it[burke40kDays] ?: "0,1,2,3,4,5,6" }
    val burke40kFromHourFlow: Flow<Int> = context.settingsDataStore.data.map { it[burke40kFromHour] ?: 9 }
    val burke40kFromMinuteFlow: Flow<Int> = context.settingsDataStore.data.map { it[burke40kFromMinute] ?: 0 }
    val burke40kToHourFlow: Flow<Int> = context.settingsDataStore.data.map { it[burke40kToHour] ?: 23 }
    val burke40kToMinuteFlow: Flow<Int> = context.settingsDataStore.data.map { it[burke40kToMinute] ?: 0 }

    val burkeAnnouncementsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[burkeAnnouncementsEnabled] ?: true }
    val modAnnouncementsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[modAnnouncementsEnabled] ?: true }
    val specialEventsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[specialEventsEnabled] ?: true }
    val tidingsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[tidingsEnabled] ?: true }
    val youtubeVideosEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[youtubeVideosEnabled] ?: true }
    val youtubeShortsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[youtubeShortsEnabled] ?: true }
    val tiktokVideosEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[tiktokVideosEnabled] ?: true }
    val twitterPostsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[twitterPostsEnabled] ?: true }

    val giveawayPopupsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[giveawayPopupsEnabled] ?: true }
    val pirateThemeEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[pirateThemeEnabled] ?: true }
    val presentationModeFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[presentationMode] ?: false }
    val showCaptainsDispatchFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[showCaptainsDispatch] ?: false }
    val pushHasAskedPermissionFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[pushHasAskedPermission] ?: false }
    val debugOverrideInteractionsDisabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[debugOverrideInteractionsDisabled] ?: false }
    val debugUseTestOverlayFlow: Flow<Boolean> = context.settingsDataStore.data.map { it[debugUseTestOverlay] ?: false }

    // --- Setters ---

    suspend fun setBurkeStreamEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[burkeStreamEnabled] = enabled }
        AppLogger.log("Settings: burkeStream.enabled = $enabled")
    }

    suspend fun setBurkeStreamAllDay(allDay: Boolean) {
        context.settingsDataStore.edit { it[burkeStreamAllDay] = allDay }
    }

    suspend fun setBurkeStreamDays(days: String) {
        context.settingsDataStore.edit { it[burkeStreamDays] = days }
    }

    suspend fun setBurkeStreamFromHour(hour: Int) {
        context.settingsDataStore.edit { it[burkeStreamFromHour] = hour }
    }

    suspend fun setBurkeStreamFromMinute(minute: Int) {
        context.settingsDataStore.edit { it[burkeStreamFromMinute] = minute }
    }

    suspend fun setBurkeStreamToHour(hour: Int) {
        context.settingsDataStore.edit { it[burkeStreamToHour] = hour }
    }

    suspend fun setBurkeStreamToMinute(minute: Int) {
        context.settingsDataStore.edit { it[burkeStreamToMinute] = minute }
    }

    suspend fun setBurke40kEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[burke40kEnabled] = enabled }
        AppLogger.log("Settings: burke40k.enabled = $enabled")
    }

    suspend fun setBurke40kAllDay(allDay: Boolean) {
        context.settingsDataStore.edit { it[burke40kAllDay] = allDay }
    }

    suspend fun setBurke40kDays(days: String) {
        context.settingsDataStore.edit { it[burke40kDays] = days }
    }

    suspend fun setBurke40kFromHour(hour: Int) {
        context.settingsDataStore.edit { it[burke40kFromHour] = hour }
    }

    suspend fun setBurke40kFromMinute(minute: Int) {
        context.settingsDataStore.edit { it[burke40kFromMinute] = minute }
    }

    suspend fun setBurke40kToHour(hour: Int) {
        context.settingsDataStore.edit { it[burke40kToHour] = hour }
    }

    suspend fun setBurke40kToMinute(minute: Int) {
        context.settingsDataStore.edit { it[burke40kToMinute] = minute }
    }

    suspend fun setBurkeAnnouncementsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[burkeAnnouncementsEnabled] = enabled }
        AppLogger.log("Settings: burkeAnnouncements = $enabled")
    }

    suspend fun setModAnnouncementsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[modAnnouncementsEnabled] = enabled }
        AppLogger.log("Settings: modAnnouncements = $enabled")
    }

    suspend fun setSpecialEventsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[specialEventsEnabled] = enabled }
        AppLogger.log("Settings: specialEvents = $enabled")
    }

    suspend fun setTidingsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[tidingsEnabled] = enabled }
        AppLogger.log("Settings: tidings = $enabled")
    }

    suspend fun setYoutubeVideosEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[youtubeVideosEnabled] = enabled }
        AppLogger.log("Settings: youtubeVideos = $enabled")
    }

    suspend fun setYoutubeShortsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[youtubeShortsEnabled] = enabled }
        AppLogger.log("Settings: youtubeShorts = $enabled")
    }

    suspend fun setTiktokVideosEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[tiktokVideosEnabled] = enabled }
        AppLogger.log("Settings: tiktokVideos = $enabled")
    }

    suspend fun setTwitterPostsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[twitterPostsEnabled] = enabled }
        AppLogger.log("Settings: twitterPosts = $enabled")
    }

    suspend fun setGiveawayPopupsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[giveawayPopupsEnabled] = enabled }
        AppLogger.log("Settings: giveawayPopups = $enabled")
    }

    suspend fun setPirateThemeEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[pirateThemeEnabled] = enabled }
        AppLogger.log("Settings: pirateTheme = $enabled")
    }

    suspend fun setPresentationMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[presentationMode] = enabled }
        AppLogger.log("Settings: presentationMode = $enabled")
    }

    suspend fun setShowCaptainsDispatch(enabled: Boolean) {
        context.settingsDataStore.edit { it[showCaptainsDispatch] = enabled }
        AppLogger.log("Settings: showCaptainsDispatch = $enabled")
    }

    suspend fun setPushHasAskedPermission(asked: Boolean) {
        context.settingsDataStore.edit { it[pushHasAskedPermission] = asked }
        AppLogger.log("Settings: pushHasAskedPermission = $asked")
    }

    suspend fun setDebugOverrideInteractionsDisabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[debugOverrideInteractionsDisabled] = enabled }
        AppLogger.log("Settings: debugOverrideInteractionsDisabled = $enabled")
    }

    suspend fun setDebugUseTestOverlay(enabled: Boolean) {
        context.settingsDataStore.edit { it[debugUseTestOverlay] = enabled }
        AppLogger.log("Settings: debugUseTestOverlay = $enabled")
    }

    suspend fun getDebugUseTestOverlay(): Boolean {
        return context.settingsDataStore.data.first()[debugUseTestOverlay] ?: false
    }

    suspend fun getDebugOverrideInteractionsDisabled(): Boolean {
        return context.settingsDataStore.data.first()[debugOverrideInteractionsDisabled] ?: false
    }

    // --- Sync to backend ---

    suspend fun buildPreferencesJson(): kotlinx.serialization.json.JsonObject {
        val prefs = context.settingsDataStore.data.first()

        val burkeSchedule = buildJsonObject {
            put("days", parseDays(prefs[burkeStreamDays] ?: "0,1,2,3,4,5,6"))
            put("from_hour", prefs[burkeStreamFromHour] ?: 9)
            put("from_min", prefs[burkeStreamFromMinute] ?: 0)
            put("to_hour", prefs[burkeStreamToHour] ?: 23)
            put("to_min", prefs[burkeStreamToMinute] ?: 0)
            put("all_day", prefs[burkeStreamAllDay] ?: true)
        }

        val burke40kSchedule = buildJsonObject {
            put("days", parseDays(prefs[burke40kDays] ?: "0,1,2,3,4,5,6"))
            put("from_hour", prefs[burke40kFromHour] ?: 9)
            put("from_min", prefs[burke40kFromMinute] ?: 0)
            put("to_hour", prefs[burke40kToHour] ?: 23)
            put("to_min", prefs[burke40kToMinute] ?: 0)
            put("all_day", prefs[burke40kAllDay] ?: true)
        }

        return buildJsonObject {
            put("notif_burkeblack_stream", if (prefs[burkeStreamEnabled] != false) 1 else 0)
            put("notif_burke40k_stream", if (prefs[burke40kEnabled] != false) 1 else 0)
            put("notif_burke_announcements", if (prefs[burkeAnnouncementsEnabled] != false) 1 else 0)
            put("notif_mod_announcements", if (prefs[modAnnouncementsEnabled] != false) 1 else 0)
            put("notif_special_events", if (prefs[specialEventsEnabled] != false) 1 else 0)
            put("notif_channel_tidings", if (prefs[tidingsEnabled] != false) 1 else 0)
            put("notif_youtube_videos", if (prefs[youtubeVideosEnabled] != false) 1 else 0)
            put("notif_youtube_shorts", if (prefs[youtubeShortsEnabled] != false) 1 else 0)
            put("notif_tiktok_videos", if (prefs[tiktokVideosEnabled] != false) 1 else 0)
            put("notif_twitter_posts", if (prefs[twitterPostsEnabled] != false) 1 else 0)
            put("burke_stream_schedule_json", burkeSchedule.toString())
            put("burke40k_stream_schedule_json", burke40kSchedule.toString())
            put("timezone", TimeZone.getDefault().id)
        }
    }

    private fun parseDays(daysString: String): kotlinx.serialization.json.JsonArray {
        val days = daysString.split(",").mapNotNull { it.trim().toIntOrNull() }
        return kotlinx.serialization.json.JsonArray(days.map { kotlinx.serialization.json.JsonPrimitive(it) })
    }

    suspend fun getGiveawayPopupsEnabled(): Boolean {
        return context.settingsDataStore.data.first()[giveawayPopupsEnabled] ?: true
    }

    suspend fun getPushHasAskedPermission(): Boolean {
        return context.settingsDataStore.data.first()[pushHasAskedPermission] ?: false
    }
}
