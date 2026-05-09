package com.swiftyspiffy.burkeblackapp.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val AVATAR_URL_KEY = stringPreferencesKey("avatar_url")
        private val IS_MODERATOR_KEY = booleanPreferencesKey("is_moderator")
        private val FORCE_VERIFY_KEY = booleanPreferencesKey("force_verify")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY] != null
    }

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val username: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY] ?: ""
    }

    val avatarUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[AVATAR_URL_KEY]
    }

    val isModerator: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_MODERATOR_KEY] ?: false
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    }

    suspend fun saveSession(
        token: String,
        userId: String,
        username: String,
        avatarUrl: String?,
        isModerator: Boolean = false
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USERNAME_KEY] = username
            avatarUrl?.let { prefs[AVATAR_URL_KEY] = it }
            prefs[IS_MODERATOR_KEY] = isModerator
        }
        AppLogger.log("Session saved for user: $username")
    }

    suspend fun updateModerator(isModerator: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_MODERATOR_KEY] = isModerator
        }
    }

    suspend fun updateAvatarUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[AVATAR_URL_KEY] = url
        }
    }

    val forceVerify: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FORCE_VERIFY_KEY] ?: false
    }

    suspend fun getForceVerify(): Boolean {
        return context.dataStore.data.first()[FORCE_VERIFY_KEY] ?: false
    }

    suspend fun clearForceVerify() {
        context.dataStore.edit { prefs ->
            prefs[FORCE_VERIFY_KEY] = false
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
            prefs[FORCE_VERIFY_KEY] = true
        }
        AppLogger.log("Session cleared, force verify set")
    }
}
