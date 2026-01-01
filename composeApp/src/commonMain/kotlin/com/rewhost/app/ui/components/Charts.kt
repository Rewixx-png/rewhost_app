package com.rewhost.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp // !!!

@Composable
fun SimpleLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color.Blue,
    maxVal: Float = 100f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (points.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1).coerceAtLeast(1)
        
        val path = Path()
        
        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value / maxVal) * height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.0f)),
                startY = 0f,
                endY = height
            )
        )
    }
}