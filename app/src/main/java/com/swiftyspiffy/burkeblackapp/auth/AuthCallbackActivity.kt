package com.swiftyspiffy.burkeblackapp.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.swiftyspiffy.burkeblackapp.BuildConfig
import com.swiftyspiffy.burkeblackapp.MainActivity
import com.swiftyspiffy.burkeblackapp.util.AppLogger

class AuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (BuildConfig.DEBUG) Log.d("AuthCallback", "Received URI: $uri")
        AppLogger.logSensitive("AuthCallback", "received: ${uri?.scheme}://${uri?.host}${uri?.path}")

        if (uri != null && uri.scheme == "burkeblackapp") {
            // Validate required auth parameters are present
            val token = uri.getQueryParameter("token")
            val username = uri.getQueryParameter("username")
            if (token != null && username != null) {
                val mainIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    data = uri
                }
                AppLogger.log("AuthCallback: valid params, launching MainActivity")
                startActivity(mainIntent)
            } else {
                if (BuildConfig.DEBUG) Log.d("AuthCallback", "Missing required auth parameters")
                AppLogger.log("AuthCallback: missing token or username in URI")
            }
        } else {
            if (BuildConfig.DEBUG) Log.d("AuthCallback", "Invalid or missing URI: $uri")
            AppLogger.log("AuthCallback: invalid URI (scheme=${uri?.scheme}, host=${uri?.host})")
        }

        finish()
    }
}
