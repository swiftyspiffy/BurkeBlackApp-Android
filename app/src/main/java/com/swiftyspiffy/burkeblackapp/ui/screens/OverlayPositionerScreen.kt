package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayPositionerScreen(
    data: PositionerData,
    streamThumbnailUrl: String,
    onDone: (OverlayPick) -> Unit,
    onBack: () -> Unit
) {
    var selectedMode by remember {
        mutableStateOf(
            if (data.modes.any { it.name == "medium" }) "medium"
            else data.modes.firstOrNull()?.name ?: "medium"
        )
    }
    var position by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var bouncePositions by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val isBounce = selectedMode == "bounce"
    val currentMode = data.modes.firstOrNull { it.name == selectedMode }

    // Bounce animation
    LaunchedEffect(isBounce) {
        if (isBounce) {
            val count = maxOf(data.bounceCount, 1)
            bouncePositions = (0 until count).map { randomPosition() }
            while (true) {
                delay(800)
                bouncePositions = (0 until count).map { randomPosition() }
            }
        } else {
            bouncePositions = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Position Overlay", fontFamily = PirateTheme.fontFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Mode selection
            Text(
                "Display Mode",
                color = PirateTheme.accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(data.modes) { mode ->
                    val isSelected = selectedMode == mode.name
                    Button(
                        onClick = { selectedMode = mode.name },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PirateTheme.accentColor else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                mode.name.replaceFirstChar { it.uppercase() },
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                "${mode.credit} cr",
                                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else PirateTheme.accentColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stream thumbnail with overlay preview
            Text(
                if (isBounce) "Bounce Preview" else "Drag to position",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A))
                    .onSizeChanged { containerSize = it }
                    .then(
                        if (!isBounce) {
                            Modifier.pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val newX = (change.position.x / containerSize.width).coerceIn(0.05f, 0.95f)
                                    val newY = (change.position.y / containerSize.height).coerceIn(0.05f, 0.95f)
                                    position = Offset(newX, newY)
                                }
                            }
                        } else Modifier
                    )
            ) {
                // Stream thumbnail background
                AsyncImage(
                    model = streamThumbnailUrl,
                    contentDescription = "Stream preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark overlay for better visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                val overlayFraction = when (selectedMode) {
                    "large" -> 0.18f
                    "medium" -> 0.12f
                    "small" -> 0.07f
                    "bounce" -> 0.06f
                    else -> 0.12f
                }

                if (isBounce) {
                    // Multiple bouncing artifacts
                    bouncePositions.forEach { bp ->
                        val animatedPos by animateOffsetAsState(
                            targetValue = bp,
                            animationSpec = tween(700),
                            label = "bounce"
                        )
                        OverlayArtifact(
                            imageUrl = data.imageURL,
                            fraction = overlayFraction,
                            containerSize = containerSize,
                            position = animatedPos
                        )
                    }
                } else {
                    // Single draggable artifact
                    OverlayArtifact(
                        imageUrl = data.imageURL,
                        fraction = overlayFraction,
                        containerSize = containerSize,
                        position = position
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Done button
            Button(
                onClick = {
                    val pick = OverlayPick(
                        imageId = data.imageId,
                        gifToken = data.gifToken,
                        name = data.name,
                        mode = selectedMode,
                        duration = 10.0,
                        xPercent = position.x.toDouble(),
                        yPercent = position.y.toDouble(),
                        credit = currentMode?.credit ?: 1
                    )
                    onDone(pick)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PirateTheme.accentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    "Done",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun OverlayArtifact(
    imageUrl: String?,
    fraction: Float,
    containerSize: IntSize,
    position: Offset
) {
    if (containerSize.width == 0) return
    val sizePx = (containerSize.width * fraction).roundToInt()
    val xPx = (position.x * containerSize.width - sizePx / 2f).roundToInt()
    val yPx = (position.y * containerSize.height - sizePx / 2f).roundToInt()

    Box(
        modifier = Modifier
            .offset { IntOffset(xPx, yPx) }
            .size(with(LocalDensity.current) { sizePx.toDp() })
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Overlay preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PirateTheme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            )
        }
    }
}

private fun randomPosition(): Offset {
    return Offset(
        x = (0.15f + Math.random().toFloat() * 0.7f),
        y = (0.15f + Math.random().toFloat() * 0.7f)
    )
}
