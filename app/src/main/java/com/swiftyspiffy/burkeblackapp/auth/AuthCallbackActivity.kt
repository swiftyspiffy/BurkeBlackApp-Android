package com.swiftyspiffy.burkeblackapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.swiftyspiffy.burkeblackapp.MainActivity

class AuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        // Never log callback URIs: they carry app credentials. Validation and
        // one-use state consumption happen once in MainActivity/AuthManager.
        if (uri != null && uri.scheme == "burkeblackapp" && uri.host == "auth" && uri.path.isNullOrEmpty()) {
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                data = uri
            }
            startActivity(mainIntent)
        }

        finish()
    }
}
