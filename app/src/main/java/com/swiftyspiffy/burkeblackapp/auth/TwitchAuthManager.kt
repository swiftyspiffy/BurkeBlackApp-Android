package com.swiftyspiffy.burkeblackapp.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.swiftyspiffy.burkeblackapp.util.AppLogger

object TwitchAuthManager {
    private const val CLIENT_ID = "jovw06nlsgfkmify8c6emwsvwo54fe"
    private const val REDIRECT_URI = "https://api.burkeblack.tv/app/auth/callback"
    private const val SCOPES = "user:read:email user:read:follows user:read:subscriptions"

    fun buildAuthUrl(forceVerify: Boolean = false): String {
        val builder = Uri.Builder()
            .scheme("https")
            .authority("id.twitch.tv")
            .appendPath("oauth2")
            .appendPath("authorize")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", SCOPES)

        if (forceVerify) {
            builder.appendQueryParameter("force_verify", "true")
        }

        return builder.build().toString()
    }

    fun launchAuth(context: Context, forceVerify: Boolean = false) {
        AppLogger.log("Auth: launching Twitch OAuth (forceVerify=$forceVerify)")
        val url = buildAuthUrl(forceVerify)
        val colorScheme = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(0xFF121212.toInt())
            .setNavigationBarColor(0xFF121212.toInt())
            .build()
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setDefaultColorSchemeParams(colorScheme)
            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    fun parseCallbackUri(uri: Uri): AuthCallbackData? {
        val error = uri.getQueryParameter("error")
        if (error != null) {
            AppLogger.log("Auth: OAuth callback error: $error")
            return null
        }

        val token = uri.getQueryParameter("token")
        val userId = uri.getQueryParameter("user_id")
        val username = uri.getQueryParameter("username")
        if (token == null || userId == null || username == null) {
            AppLogger.log("Auth: callback missing params (token=${token != null}, userId=${userId != null}, username=${username != null})")
            return null
        }
        val avatarUrl = uri.getQueryParameter("avatar_url")

        AppLogger.logSensitive("Auth", "callback parsed for user=$username")
        return AuthCallbackData(
            token = token,
            userId = userId,
            username = username,
            avatarUrl = avatarUrl
        )
    }
}

data class AuthCallbackData(
    val token: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?
)
