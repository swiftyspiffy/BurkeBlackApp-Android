package com.swiftyspiffy.burkeblackapp.widget

import android.graphics.Color
import androidx.glance.unit.ColorProvider

object WidgetColors {
    val gold = Color.rgb(0xCC, 0x88, 0x03)
    val darkBrown = Color.rgb(0x1A, 0x12, 0x0A)
    val medBrown = Color.rgb(0x29, 0x1E, 0x14)
    val white = Color.WHITE
    val muted = Color.rgb(0x99, 0x99, 0x99)
    val red = Color.rgb(0xEF, 0x53, 0x50)
    val liveRed = Color.rgb(0xFF, 0x00, 0x00)

    val goldProvider = ColorProvider(gold)
    val darkBrownProvider = ColorProvider(darkBrown)
    val medBrownProvider = ColorProvider(medBrown)
    val whiteProvider = ColorProvider(white)
    val mutedProvider = ColorProvider(muted)
    val redProvider = ColorProvider(red)
    val liveRedProvider = ColorProvider(liveRed)
}
