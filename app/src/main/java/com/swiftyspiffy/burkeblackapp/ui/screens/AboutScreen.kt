package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.BuildConfig
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onNavigateToFeedback: (() -> Unit)? = null,
    onNavigateToAdvanced: (() -> Unit)? = null
) {
    LaunchedEffect(Unit) { AppLogger.log("About: appeared") }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PirateTheme.accentColor
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "About",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 22.sp,
                    color = PirateTheme.accentColor
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App info card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_icon),
                            contentDescription = "The Dirty Skull",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "The Dirty Skull",
                            color = PirateTheme.accentColor,
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 24.sp
                        )

                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME}",
                            color = PirateTheme.accentColor.copy(alpha = 0.5f),
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "The official companion app for BurkeBlack's Twitch community. Stay up to date with streams, view your stats, and connect across all platforms.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Developer section
                Text(
                    text = "Ship's Engineer",
                    color = PirateTheme.accentColor.copy(alpha = 0.7f),
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                    ) {
                        // Built by row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Built by",
                                color = Color.White.copy(alpha = 0.5f),
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 14.sp
                            )
                            Text(
                                "swiftyspiffy",
                                color = PirateTheme.accentColor,
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 16.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(PirateTheme.accentColor.copy(alpha = 0.1f))
                        )

                        // Social icons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { uriHandler.openUri("https://twitch.tv/swiftyspiffy") }
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_twitch),
                                    contentDescription = "Twitch",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Twitch", color = Color.White.copy(alpha = 0.5f), fontFamily = PirateTheme.fontFamily, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(32.dp))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { uriHandler.openUri("https://twitter.com/swiftyspiffy") }
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_x),
                                    contentDescription = "Twitter / X",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Twitter / X", color = Color.White.copy(alpha = 0.5f), fontFamily = PirateTheme.fontFamily, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(32.dp))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { uriHandler.openUri("https://github.com/swiftyspiffy") }
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_github),
                                    contentDescription = "GitHub",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("GitHub", color = Color.White.copy(alpha = 0.5f), fontFamily = PirateTheme.fontFamily, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Open Source
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Open Source",
                    color = PirateTheme.accentColor.copy(alpha = 0.7f),
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                    ) {
                        // iOS repo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { uriHandler.openUri("https://github.com/swiftyspiffy/BurkeBlackApp-iOS") }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PhoneIphone,
                                    contentDescription = "iOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("iOS App", color = Color.White, fontFamily = PirateTheme.fontFamily, fontSize = 15.sp)
                                Text("Swift • SwiftUI • WidgetKit", color = Color.White.copy(alpha = 0.4f), fontFamily = PirateTheme.fontFamily, fontSize = 11.sp)
                            }
                            Image(
                                painter = painterResource(R.drawable.ic_github),
                                contentDescription = "GitHub",
                                modifier = Modifier.size(20.dp),
                                alpha = 0.4f
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(PirateTheme.accentColor.copy(alpha = 0.1f))
                        )

                        // Android repo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { uriHandler.openUri("https://github.com/swiftyspiffy/BurkeBlackApp-Android") }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Green.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Android,
                                    contentDescription = "Android",
                                    tint = Color.Green,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Android App", color = Color.White, fontFamily = PirateTheme.fontFamily, fontSize = 15.sp)
                                Text("Kotlin • Jetpack Compose", color = Color.White.copy(alpha = 0.4f), fontFamily = PirateTheme.fontFamily, fontSize = 11.sp)
                            }
                            Image(
                                painter = painterResource(R.drawable.ic_github),
                                contentDescription = "GitHub",
                                modifier = Modifier.size(20.dp),
                                alpha = 0.4f
                            )
                        }
                    }
                }

                // Send Feedback
                if (onNavigateToFeedback != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    AboutActionCard(
                        icon = Icons.Default.Email,
                        title = "Send Feedback",
                        subtitle = "Report a bug or share yer thoughts",
                        onClick = onNavigateToFeedback
                    )
                }

                // Privacy Policy
                Spacer(modifier = Modifier.height(12.dp))

                AboutActionCard(
                    icon = Icons.Default.Policy,
                    title = "Privacy Policy",
                    subtitle = "The ship's articles & terms",
                    onClick = { uriHandler.openUri("https://burkeblack.tv/app/privacy/") }
                )

                // Debug section
                if (onNavigateToAdvanced != null) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Below Deck",
                        color = PirateTheme.accentColor.copy(alpha = 0.7f),
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, bottom = 8.dp)
                    )

                    AboutActionCard(
                        icon = Icons.Default.Build,
                        title = "Advanced",
                        subtitle = "Debug tools & diagnostics",
                        onClick = onNavigateToAdvanced
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            } // end inner Column
        }
    }
}

@Composable
private fun AboutActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PirateTheme.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PirateTheme.accentColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily, fontSize = 18.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontFamily = PirateTheme.fontFamily, fontSize = 12.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = PirateTheme.accentColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
