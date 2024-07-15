package com.example.smartcamera.objectdetector

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.ui.viewmodel.ObjectFocusedOnState
import com.google.mlkit.vision.objects.DetectedObject
import kotlin.math.max
import kotlin.math.min

class ObjectGraphic(
    private val toggleTrackedTrackingId: (Int) -> Unit,
    private val setGuideModeOn: (Boolean) -> Unit,
    val detectedObject: DetectedObject,
    private val viewModel: MainViewModel
) {
    private val numColors = COLORS.size
    private val boxPaints = Array(numColors) { Paint() }
    private val textPaints = Array(numColors) { Paint() }
    private val labelPaints = Array(numColors) { Paint() }

    init {
        for (i in 0 until numColors) {
            textPaints[i] = Paint().apply {
                color = COLORS[i][0].toArgb()
                textSize = TEXT_SIZE
            }
            boxPaints[i] = Paint().apply {
                color = COLORS[i][1].toArgb()
                style = Paint.Style.STROKE
                strokeWidth = STROKE_WIDTH
            }
            labelPaints[i] = Paint().apply {
                color = COLORS[i][1].toArgb()
                style = Paint.Style.FILL
            }
        }
    }

    fun draw(drawScope: DrawScope, state: ObjectFocusedOnState) {
        val rect = RectF(detectedObject.boundingBox)
        val x0 = state.translateX(rect.left)
        val x1 = state.translateX(rect.right)
        rect.left = min(x0, x1)
        rect.right = max(x0, x1)
        rect.top = state.translateY(rect.top)
        rect.bottom = state.translateY(rect.bottom)

        if (detectedObject.trackingId in state.trackedTrackingIds) {
            val cornerSize = 60f
            val strokeWidth = 5f
            val colorID = 7 // 노란색 사용

            viewModel.setFocusPoint(rect.centerX(), rect.centerY())
            viewModel.updateTrackingId(detectedObject.trackingId!!)

            // 모서리 그리기 로직
            drawCorners(drawScope, rect, cornerSize, strokeWidth, Color.Yellow)
        }
    }

    private fun drawCorners(drawScope: DrawScope, rect: RectF, cornerSize: Float, strokeWidth: Float, color: Color) {
        // 왼쪽 위 모서리
        drawScope.drawLine(
            color = color,
            start = Offset(rect.left, rect.top),
            end = Offset(rect.left + cornerSize, rect.top),
            strokeWidth = strokeWidth
        )
        drawScope.drawLine(
            color = color,
            start = Offset(rect.left, rect.top),
            end = Offset(rect.left, rect.top + cornerSize),
            strokeWidth = strokeWidth
        )

        // 오른쪽 위 모서리
        drawScope.drawLine(
            color = color,
            start = Offset(rect.right, rect.top),
            end = Offset(rect.right - cornerSize, rect.top),
            strokeWidth = strokeWidth
        )
        drawScope.drawLine(
            color = color,
            start = Offset(rect.right, rect.top),
            end = Offset(rect.right, rect.top + cornerSize),
            strokeWidth = strokeWidth
        )

        // 왼쪽 아래 모서리
        drawScope.drawLine(
            color = color,
            start = Offset(rect.left, rect.bottom),
            end = Offset(rect.left + cornerSize, rect.bottom),
            strokeWidth = strokeWidth
        )
        drawScope.drawLine(
            color = color,
            start = Offset(rect.left, rect.bottom),
            end = Offset(rect.left, rect.bottom - cornerSize),
            strokeWidth = strokeWidth
        )

        // 오른쪽 아래 모서리
        drawScope.drawLine(
            color = color,
            start = Offset(rect.right, rect.bottom),
            end = Offset(rect.right - cornerSize, rect.bottom),
            strokeWidth = strokeWidth
        )
        drawScope.drawLine(
            color = color,
            start = Offset(rect.right, rect.bottom),
            end = Offset(rect.right, rect.bottom - cornerSize),
            strokeWidth = strokeWidth
        )
    }

    fun handleClick(state: ObjectFocusedOnState, clickedPoint: Offset): Boolean {
        val rect = RectF(detectedObject.boundingBox)
        val x0 = state.translateX(rect.left)
        val x1 = state.translateX(rect.right)
        rect.left = min(x0, x1)
        rect.right = max(x0, x1)
        rect.top = state.translateY(rect.top)
        rect.bottom = state.translateY(rect.bottom)

        return clickedPoint.x in rect.left..rect.right && clickedPoint.y in rect.top..rect.bottom
    }

    companion object {
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
        private const val NUM_COLORS = 10
        private val COLORS = arrayOf(
            arrayOf(Color.White, Color.Yellow),
            arrayOf(Color.White, Color.Magenta),
            arrayOf(Color.Black, Color.LightGray),
            arrayOf(Color.White, Color.Red),
            arrayOf(Color.White, Color.Blue),
            arrayOf(Color.White, Color.DarkGray),
            arrayOf(Color.Black, Color.Cyan),
            arrayOf(Color.Yellow, Color.Yellow),
            arrayOf(Color.White, Color.Black),
            arrayOf(Color.Black, Color.Green)
        )
    }
}