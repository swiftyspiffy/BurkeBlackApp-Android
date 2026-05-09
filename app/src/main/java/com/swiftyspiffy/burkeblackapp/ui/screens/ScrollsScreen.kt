package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger

@Composable
fun ScrollsScreen(
    onNavigateToStudio: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {}
) {
    val bgColor = MaterialTheme.colorScheme.background

    androidx.compose.runtime.LaunchedEffect(Unit) { AppLogger.log("Scrolls: appeared") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Studio - top half
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(onClick = onNavigateToStudio),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.bg_studio),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Fade to background at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                bgColor.copy(alpha = 0.3f),
                                bgColor
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            // Darken overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            Text(
                text = "Captain's Studio",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 36.sp,
                color = PirateTheme.accentColor
            )
        }

        // FAQ - bottom half
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(onClick = onNavigateToFaq),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.bg_faq),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Fade to background at top
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                bgColor,
                                bgColor.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            // Darken overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            Text(
                text = "Information and FAQ",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 36.sp,
                color = PirateTheme.accentColor
            )
        }
    }
}
