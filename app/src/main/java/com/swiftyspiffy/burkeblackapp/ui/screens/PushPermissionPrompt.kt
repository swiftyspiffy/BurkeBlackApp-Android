package com.swiftyspiffy.burkeblackapp.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.BurkeBlackApplication
import com.swiftyspiffy.burkeblackapp.push.PushNotificationManager
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.launch

@Composable
fun PushPermissionPrompt(
    isLoggedIn: Boolean,
    token: String?
) {
    val context = LocalContext.current
    val appSettings = (context.applicationContext as BurkeBlackApplication).appSettings
    val scope = rememberCoroutineScope()

    val hasAsked by appSettings.pushHasAskedPermissionFlow.collectAsState(initial = true) // default true to avoid flash
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        AppLogger.log("Push: permission ${if (granted) "granted" else "denied"} from prompt")
        scope.launch { appSettings.setPushHasAskedPermission(true) }
        // Register FCM token if granted
        if (granted && token != null) {
            scope.launch {
                val fcmToken = PushNotificationManager.getFcmToken()
                if (fcmToken != null) {
                    PushNotificationManager.registerWithBackend(token, fcmToken)
                }
            }
        }
    }

    LaunchedEffect(isLoggedIn, hasAsked) {
        if (isLoggedIn && !hasAsked) {
            // Check if already authorized
            if (PushNotificationManager.hasNotificationPermission(context)) {
                // Already authorized, just register silently
                AppLogger.log("Push: already authorized, registering silently")
                appSettings.setPushHasAskedPermission(true)
                if (token != null) {
                    val fcmToken = PushNotificationManager.getFcmToken()
                    if (fcmToken != null) {
                        PushNotificationManager.registerWithBackend(token, fcmToken)
                    }
                }
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // Below Android 13, notifications allowed by default
                AppLogger.log("Push: pre-Android 13, notifications allowed by default")
                appSettings.setPushHasAskedPermission(true)
                if (token != null) {
                    val fcmToken = PushNotificationManager.getFcmToken()
                    if (fcmToken != null) {
                        PushNotificationManager.registerWithBackend(token, fcmToken)
                    }
                }
            } else {
                showDialog = true
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = Color(0xFF1A1A2E),
            icon = {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Ahoy, Sailor!",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 24.sp,
                    color = PirateTheme.accentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "The Captain and his crew would like to send ye dispatches from the ship — stream alerts, announcements, special events, and news from The Dirty Skull.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Shall we hoist the signal flags? Ye can trim the sails anytime in Rigging \u2192 Notifications.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        AppLogger.log("Push: user accepted prompt")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PirateTheme.accentColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Aye, Keep Me Posted!",
                        fontFamily = PirateTheme.fontFamily,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        AppLogger.log("Push: user declined prompt")
                        scope.launch { appSettings.setPushHasAskedPermission(true) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Nay, Not Now",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}
