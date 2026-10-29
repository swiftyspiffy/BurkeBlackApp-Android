package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiftyspiffy.burkeblackapp.BurkeBlackApplication
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.data.CaptainBubblePosition
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun DraggableCaptainButton(onOpen: () -> Unit) {
    val settings = (LocalContext.current.applicationContext as BurkeBlackApplication).appSettings
    val saved: CaptainBubblePosition? by
        settings.captainBubblePositionFlow.collectAsStateWithLifecycle(initialValue = null)
    val initial = saved ?: return
    var x by rememberSaveable { mutableFloatStateOf(initial.x) }
    var y by rememberSaveable { mutableFloatStateOf(initial.y) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(saved) {
        if (!dragging) {
            x = initial.x
            y = initial.y
        }
    }
    fun save() {
        val position = CaptainBubblePosition(x, y)
        scope.launch { settings.setCaptainBubblePosition(position) }
    }
    fun moveTo(newX: Float, newY: Float): Boolean {
        x = newX
        y = newY
        save()
        return true
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val inset = with(density) { 12.dp.toPx() }
        val diameter = with(density) { 60.dp.toPx() }
        // The parent already excludes system bars and the app's bottom navigation.
        val right = (constraints.maxWidth - diameter - inset).coerceAtLeast(inset)
        val bottom = (constraints.maxHeight - diameter - inset).coerceAtLeast(inset)
        val horizontalRange = right - inset
        val verticalRange = bottom - inset
        Image(
            painter = painterResource(R.drawable.ic_burke_captain),
            contentDescription = null,
            modifier =
                Modifier.offset {
                        IntOffset(
                            (inset + horizontalRange * x).roundToInt(),
                            (inset + verticalRange * y).roundToInt(),
                        )
                    }
                    .size(60.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(PirateTheme.cardGradient)
                    .border(2.dp, PirateTheme.accentColor, CircleShape)
                    .testTag("captains.restore")
                    .semantics {
                        contentDescription = "Open Captains"
                        stateDescription = "Drag to move this button out of the way"
                        customActions =
                            listOf(
                                CustomAccessibilityAction("Move to top left") { moveTo(0f, 0f) },
                                CustomAccessibilityAction("Move to top right") { moveTo(1f, 0f) },
                                CustomAccessibilityAction("Move to bottom left") { moveTo(0f, 1f) },
                                CustomAccessibilityAction("Move to bottom right") { moveTo(1f, 1f) },
                            )
                    }
                    .pointerInput(horizontalRange, verticalRange) {
                        detectDragGestures(
                            onDragStart = { dragging = true },
                            onDragEnd = {
                                dragging = false
                                save()
                            },
                            onDragCancel = {
                                dragging = false
                                save()
                            },
                        ) { change, delta ->
                            change.consume()
                            x =
                                if (horizontalRange > 0)
                                    (x + delta.x / horizontalRange).coerceIn(0f, 1f)
                                else 0f
                            y =
                                if (verticalRange > 0)
                                    (y + delta.y / verticalRange).coerceIn(0f, 1f)
                                else 0f
                        }
                    }
                    .clickable(role = Role.Button, onClickLabel = "Open Captains", onClick = onOpen)
                    .padding(6.dp),
        )
    }
}
