package com.swiftyspiffy.burkeblackapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.swiftyspiffy.burkeblackapp.R

val PirataOne = FontFamily(
    Font(R.font.pirata_one_regular, FontWeight.Normal)
)

val LocalPirateTheme = compositionLocalOf { true }

object PirateTheme {
    val fontFamily: FontFamily
        @Composable get() = if (LocalPirateTheme.current) PirataOne else FontFamily.Default

    val accentColor: Color
        @Composable get() = if (LocalPirateTheme.current) PirateGold else Color(0xFF90CAF9)

    val cardGradient: Brush
        @Composable get() = if (LocalPirateTheme.current) {
            Brush.horizontalGradient(listOf(Color(0xFF1C1208), Color(0xFF2A1E10)))
        } else {
            Brush.horizontalGradient(listOf(DarkSurface, DarkSurface))
        }

    val iconBgColor: Color
        @Composable get() = if (LocalPirateTheme.current) Color(0xFF3D2E14) else DarkSurfaceVariant
}
