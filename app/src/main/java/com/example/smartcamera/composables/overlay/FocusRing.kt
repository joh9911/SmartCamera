package com.example.smartcamera.composables.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FocusRing(position: Offset, modifier: Modifier = Modifier) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(position) {
        isVisible = true
        delay(2000) // 2초 후에 사라짐
        isVisible = false
    }

    if (isVisible) {
        Canvas(
            modifier = modifier
        ) {
            val radius = 40f
            drawCircle(
                color = Color.White,
                radius = radius,
                center = position,
                style = Stroke(width = 2f)
            )

            // Draw left sun icon
            drawSunIcon(Offset(position.x - radius - 24, position.y))

            // Draw right sun icon
            drawSunIcon(Offset(position.x + radius + 24, position.y))
        }
    }
}

private fun DrawScope.drawSunIcon(center: Offset) {
    val radius = 8f

    // Draw sun circle
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center
    )

    // Draw sun rays
    for (i in 0 until 8) {
        val angle = i * (2 * PI / 8)
        val startX = center.x + cos(angle).toFloat() * radius
        val startY = center.y + sin(angle).toFloat() * radius
        val endX = center.x + cos(angle).toFloat() * (radius + 4)
        val endY = center.y + sin(angle).toFloat() * (radius + 4)

        drawLine(
            color = Color.White,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2f
        )
    }
}
