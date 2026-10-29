package com.swiftyspiffy.burkeblackapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.ui.theme.LocalPirateTheme
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Modifier.pirateScreenBackground(): Modifier {
    val accent = PirateTheme.accentColor
    return background(Color.Black).drawWithCache {
        val glow =
            Brush.radialGradient(
                listOf(accent.copy(alpha = 0.09f), Color.Transparent),
                center = Offset(size.width / 2, 0f),
                radius = 560.dp.toPx(),
            )
        onDrawBehind { drawRect(glow) }
    }
}

/** Cached, purely decorative chart lines, compass, and treasure route. */
fun Modifier.pirateTexture(accent: Color, intensity: Float = 0.045f): Modifier = drawWithCache {
    val lines =
        Path().apply {
            var offset = -size.height
            while (offset < size.width) {
                moveTo(offset, 0f)
                lineTo(offset + size.height, size.height)
                offset += 30.dp.toPx()
            }
        }
    val compassCenter = Offset(size.width * 0.82f, size.height * 0.24f)
    val radius = maxOf(18.dp.toPx(), size.minDimension * 0.2f)
    val compass =
        Path().apply {
            repeat(8) { point ->
                val angle = point * Math.PI / 4
                moveTo(
                    compassCenter.x + cos(angle).toFloat() * radius * 0.22f,
                    compassCenter.y + sin(angle).toFloat() * radius * 0.22f,
                )
                lineTo(
                    compassCenter.x + cos(angle).toFloat() * radius,
                    compassCenter.y + sin(angle).toFloat() * radius,
                )
            }
        }
    val route =
        Path().apply {
            moveTo(size.width * 0.05f, size.height * 0.78f)
            cubicTo(
                size.width * 0.28f,
                size.height * 0.5f,
                size.width * 0.5f,
                size.height * 0.9f,
                size.width * 0.72f,
                size.height * 0.62f,
            )
        }
    val marker = Offset(size.width * 0.72f, size.height * 0.62f)
    val markerSize = 4.dp.toPx()
    val routeStroke =
        Stroke(
            0.9.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 5.dp.toPx())),
        )
    onDrawBehind {
        val ink = accent.copy(alpha = intensity)
        drawPath(lines, accent.copy(alpha = intensity * 0.55f), style = Stroke(0.6.dp.toPx()))
        drawCircle(ink, radius, compassCenter, style = Stroke(0.8.dp.toPx()))
        drawCircle(ink, radius * 0.38f, compassCenter, style = Stroke(0.8.dp.toPx()))
        drawPath(compass, ink, style = Stroke(0.8.dp.toPx()))
        drawPath(route, ink, style = routeStroke)
        drawLine(
            ink,
            marker - Offset(markerSize, markerSize),
            marker + Offset(markerSize, markerSize),
            1.dp.toPx(),
        )
        drawLine(
            ink,
            marker + Offset(-markerSize, markerSize),
            marker + Offset(markerSize, -markerSize),
            1.dp.toPx(),
        )
    }
}

@Composable
fun Modifier.pirateCardSurface(
    cornerRadius: Dp = 16.dp,
    textureIntensity: Float = 0.045f,
    highlighted: Boolean = false,
): Modifier {
    val accent = PirateTheme.accentColor
    val shape = RoundedCornerShape(cornerRadius)
    val texture = if (LocalPirateTheme.current) textureIntensity else 0f
    return shadow(
            if (highlighted) 10.dp else 8.dp,
            shape,
            ambientColor = if (highlighted) accent else Color.Black,
            spotColor = if (highlighted) accent.copy(alpha = 0.35f) else Color.Black,
        )
        .clip(shape)
        .background(PirateTheme.cardGradient)
        .pirateTexture(accent, texture)
        .border(
            if (highlighted) 1.5.dp else 1.dp,
            Brush.linearGradient(
                listOf(
                    accent.copy(alpha = if (highlighted) 0.85f else 0.3f),
                    if (highlighted) accent.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.08f),
                )
            ),
            shape,
        )
}

@Composable
fun PirateCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 16.dp,
    textureIntensity: Float = 0.045f,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, label = "cardPress")
    Column(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pirateCardSurface(cornerRadius, textureIntensity, highlighted)
            .then(
                if (onClick != null)
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = androidx.compose.material3.ripple(),
                        role = Role.Button,
                        onClick = onClick,
                    )
                else Modifier
            ),
        content = content,
    )
}

@Composable
fun PiratePageHeader(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    val accent = PirateTheme.accentColor
    Column(
        modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp).semantics(
            mergeDescendants = true
        ) {
            heading()
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeaderLine(Modifier.weight(1f), accent, false)
            Text(
                title,
                fontFamily = PirateTheme.fontFamily,
                fontSize = 34.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 260.dp),
            )
            HeaderLine(Modifier.weight(1f), accent, true)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(Modifier.width(38.dp).height(1.dp).background(accent.copy(alpha = 0.4f)))
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
            Box(Modifier.width(38.dp).height(1.dp).background(accent.copy(alpha = 0.4f)))
        }
        if (subtitle != null)
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
    }
}

@Composable
private fun HeaderLine(modifier: Modifier, accent: Color, reversed: Boolean) {
    Box(modifier, contentAlignment = if (reversed) Alignment.CenterStart else Alignment.CenterEnd) {
        Box(
            Modifier.widthIn(max = 70.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        if (reversed) listOf(accent.copy(alpha = 0.65f), Color.Transparent)
                        else listOf(Color.Transparent, accent.copy(alpha = 0.65f))
                    )
                )
        )
    }
}

@Composable
fun PirateActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconBackground: Color = PirateTheme.iconBgColor,
    icon: @Composable () -> Unit,
) {
    val accent = PirateTheme.accentColor
    PirateCard(modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier.size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBackground)
                    .border(1.dp, accent.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontFamily = PirateTheme.fontFamily, fontSize = 20.sp, color = accent)
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }
            Box(
                Modifier.size(28.dp).background(accent.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = accent.copy(alpha = 0.72f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
