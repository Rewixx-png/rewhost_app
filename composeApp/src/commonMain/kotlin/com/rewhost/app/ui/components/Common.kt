package com.rewhost.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rewhost.app.ui.theme.*

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617)))
    )) { content() }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Slate800.copy(alpha = 0.5f),
    borderColor: Color = GlassBorder, // Вот этот параметр терялся
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var mod = modifier.clip(shape).background(backgroundColor).border(1.dp, borderColor, shape)
    if (onClick != null) mod = mod.clickable(onClick = onClick)
    Column(modifier = mod.padding(padding)) { content() }
}

@Composable
fun BouncyBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f)
    Box(modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource, null, onClick = onClick), content = content)
}