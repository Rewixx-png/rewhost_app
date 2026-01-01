package com.rewhost.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rewhost.app.utils.AppSettings
import com.rewhost.app.utils.AppTheme
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    // Стильный темный "стеклянный" фон
    val baseColor = Color(0xFF1E293B).copy(alpha = 0.7f)
    
    // Тонкая градиентная обводка сверху вниз (эффект света)
    val borderBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(baseColor)
            .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(24.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(padding)
    ) {
        content()
    }
}

@Composable
fun BouncyBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(false)
                        pressed = true
                        val up = waitForUpOrCancellation()
                        pressed = false
                        if (up != null) {
                            onClick()
                        }
                    }
                }
            }
    ) {
        content()
    }
}

@Composable
fun SnowOverlay() {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Статичная заглушка, чтобы не нагружать UI анимациями частиц на данном этапе
    }
}