package com.swiftyspiffy.burkeblackapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.swiftyspiffy.burkeblackapp.BurkeBlackApplication

private val PirateDarkColorScheme = darkColorScheme(
    primary = PirateGold,
    onPrimary = Color.Black,
    secondary = PirateDark,
    tertiary = TwitchPurple,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),
)

private val StandardDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color.Black,
    secondary = Color(0xFF546E7A),
    tertiary = TwitchPurple,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),
)

@Composable
fun BurkeBlackAppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val appSettings = (context.applicationContext as BurkeBlackApplication).appSettings
    val pirateTheme by appSettings.pirateThemeEnabledFlow.collectAsState(initial = true)

    val colorScheme = if (pirateTheme) PirateDarkColorScheme else StandardDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalPirateTheme provides pirateTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
