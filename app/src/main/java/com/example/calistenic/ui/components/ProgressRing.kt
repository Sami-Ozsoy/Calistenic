package com.example.calistenic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dairesel ilerleme halkası. progress 0..1 arası; 1 = tam dolu.
 */
@Composable
fun ProgressRing(
    modifier: Modifier = Modifier,
    progress: Float,
    color: Color,
    trackColor: Color = Color.Transparent,
    strokeWidth: Dp = 14.dp
) {
    val strokePx = strokeWidth.value
    Canvas(modifier = modifier) {
        val stroke = strokePx * density
        val diameter = (size.minDimension - stroke)
        val topLeft = Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)

        // Track (arka plan halkası)
        if (trackColor != Color.Transparent) {
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Progress halkası — üstten başlar, saat yönünde
        val sweep = 360f * progress.coerceIn(0f, 1f)
        if (sweep > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}