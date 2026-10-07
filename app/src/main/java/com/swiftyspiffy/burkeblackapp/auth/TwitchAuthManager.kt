package com.swiftyspiffy.burkeblackapp.auth

import android.content.Context
import com.swiftyspiffy.burkeblackapp.BuildConfig
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.swiftyspiffy.burkeblackapp.util.AppLogger

object TwitchAuthManager {
    private const val CLIENT_ID = "jovw06nlsgfkmify8c6emwsvwo54fe"
    private const val REDIRECT_URI = "https://api.burkeblack.tv/app/auth/callback"
    private const val SCOPES = "user:read:email user:read:follows user:read:subscriptions"

    private fun buildAuthUrl(context: Context, forceVerify: Boolean = false): String {
        if (BuildConfig.GO_API_AUTH) {
            val nonce = OAuthAttempt.nonce()
            context.getSharedPreferences("oauth_attempt", Context.MODE_PRIVATE).edit()
                .putString("nonce", nonce).putLong("started", System.currentTimeMillis()).commit()
            return Uri.parse("https://api.burkeblack.tv/app/auth/start").buildUpon()
                .appendQueryParameter("client_state", nonce)
                .appendQueryParameter("force_verify", forceVerify.toString()).build().toString()
        }
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
        val url = buildAuthUrl(context, forceVerify)
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

    @Synchronized
    fun parseCallbackUri(context: Context, uri: Uri): AuthCallbackData? {
        if (uri.scheme != "burkeblackapp" || uri.host != "auth" || !uri.path.isNullOrEmpty() || uri.fragment != null || uri.userInfo != null || uri.port != -1) return null
        val fields = listOf("state", "token", "user_id", "username", "avatar_url", "error")
        if (fields.any { uri.getQueryParameters(it).size > 1 }) return null
        if (BuildConfig.GO_API_AUTH) {
            val prefs = context.getSharedPreferences("oauth_attempt", Context.MODE_PRIVATE)
            if (!OAuthAttempt.matches(prefs.getString("nonce", null), uri.getQueryParameters("state"), prefs.getLong("started", 0), System.currentTimeMillis())) {
                AppLogger.log("Auth: rejected invalid or expired sign-in state")
                return null
            }
            // Consume on success and controlled error; persist before accepting a token.
            if (!prefs.edit().clear().commit()) return null
        }
        val error = uri.getQueryParameter("error")
        if (error != null) {
            AppLogger.log("Auth: authorization was not completed")
            return null
        }

        val token = uri.getQueryParameter("token")
        val userId = uri.getQueryParameter("user_id")
        val username = uri.getQueryParameter("username")
        if (token.isNullOrEmpty() || userId.isNullOrEmpty() || username.isNullOrEmpty()) {
            AppLogger.log("Auth: callback missing params (token=${token != null}, userId=${userId != null}, username=${username != null})")
            return null
        }
        val avatarUrl = uri.getQueryParameter("avatar_url")

        AppLogger.log("Auth: sign-in callback accepted")
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
