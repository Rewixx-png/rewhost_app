package com.rewhost.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

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
        // Пропускаем первую точку, если их много, для плавности
        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value / maxVal) * height)
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height) // Start bottom
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Close fill path
        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw Fill Gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.0f)),
                startY = 0f,
                endY = height
            )
        )

        // Draw Line
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}