package com.example.smartcamera.objectdetector

import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.ui.viewmodel.ObjectFocusedOnState
import com.example.smartcamera.utils.Tag
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
        val colorID = 0

        val rect = RectF(detectedObject.boundingBox)
        val x0 = state.translateX(rect.left)
        val x1 = state.translateX(rect.right)
        rect.left = min(x0, x1)
        rect.right = max(x0, x1)
        rect.top = state.translateY(rect.top)
        rect.bottom = state.translateY(rect.bottom)

        val cornerSize = 60f
        val strokeWidth = 5f

        state.clickedPoint?.let { point ->
            Log.d(Tag.TAG,"클릭한 포인트 확인")
            val clickedObjects = state.graphics.filterIsInstance<ObjectGraphic>().filter { graphic ->
                val objRect = RectF(graphic.detectedObject.boundingBox)
                val objX0 = state.translateX(objRect.left)
                val objX1 = state.translateX(objRect.right)
                objRect.left = min(objX0, objX1)
                objRect.right = max(objX0, objX1)
                objRect.top = state.translateY(objRect.top)
                objRect.bottom = state.translateY(objRect.bottom)
                point.x in objRect.left..objRect.right && point.y in objRect.top..objRect.bottom
            }
            // 가장 작은 객체를 선택합니다.
            val smallestObject = clickedObjects.minByOrNull { graphic ->
                val objRect = graphic.detectedObject.boundingBox
                objRect.width() * objRect.height()
            }

            // 선택된 객체가 있다면, 해당 객체의 trackingId를 설정합니다.
            smallestObject?.let {
                toggleTrackedTrackingId(it.detectedObject.trackingId!!)
            }
        }

        // trackedTrackingIds에 있는 객체들만 박스를 그립니다.
        if (detectedObject.trackingId in state.trackedTrackingIds) {
            val cornerSize = 60f
            val strokeWidth = 5f
            val colorID = 7 // 노란색 사용

            viewModel.setFocusPoint(rect.centerX(), rect.centerY())
            viewModel.updateTrackingId(detectedObject.trackingId!!) // trackingId 갱신


            // 왼쪽 위 모서리
            drawScope.drawLine(
                color = Color.Yellow,
                start = Offset(rect.left, rect.top),
                end = Offset(rect.left + cornerSize, rect.top),
                strokeWidth = strokeWidth
            )
            drawScope.drawLine(
                color = Color.Yellow,
                start = Offset(rect.left, rect.top),
                end = Offset(rect.left, rect.top + cornerSize),
                strokeWidth = strokeWidth
            )

            // 오른쪽 위 모서리
            drawScope.drawLine(
                color = Color.Yellow,
                start = Offset(rect.right, rect.top),
                end = Offset(rect.right - cornerSize, rect.top),
                strokeWidth = strokeWidth
            )
            drawScope.drawLine(
                color = Color.Yellow,
                start = Offset(rect.right, rect.top),
                end = Offset(rect.right, rect.top + cornerSize),
                strokeWidth = strokeWidth
            )

            // 왼쪽 아래 모서리
            drawScope.drawLine(
                color = Color.Yellow,
                start = Offset(rect.left, rect.bottom),
                end = Offset(rect.left + cornerSize, rect.bottom),
                strokeWidth = strokeWidth
            )
            drawScope.drawLine(
                color = Color.Yellow,
                start = Offset(rect.left, rect.bottom),
                end = Offset(rect.left, rect.bottom - cornerSize),
                strokeWidth = strokeWidth
            )

            // 오른쪽 아래 모서리
            drawScope.drawLine(
                color = Color.Yellow,
                start = Offset(rect.right, rect.bottom),
                end = Offset(rect.right - cornerSize, rect.bottom),
                strokeWidth = strokeWidth
            )
            drawScope.drawLine(
                color = Color.Yellow,
                start = Offset(rect.right, rect.bottom),
                end = Offset(rect.right, rect.bottom - cornerSize),
                strokeWidth = strokeWidth
            )
        }



//        var yLabelOffset = rect.top - 20
//        drawText(drawScope, "Tracking ID: ${detectedObject.trackingId}", rect.left, yLabelOffset, textPaints[colorID])
//
//        yLabelOffset += 54f + 4f
//        for (label in detectedObject.labels) {
//            drawText(drawScope, label.text, rect.left, yLabelOffset, textPaints[2])
//            yLabelOffset += 54f + 4f
//            drawText(drawScope, String.format("%.2f%% confidence (index: %d)", label.confidence * 100, label.index), rect.left, yLabelOffset, textPaints[2])
//            yLabelOffset += 54f + 4f
//        }
        // 클릭된 포인트가 객체의 영역에 속하는지 확인합니다.
//        state.clickedPoint?.let { point ->
//            if (point.x in rect.left..rect.right && point.y in rect.top..rect.bottom) {
//
//
//                // 왼쪽 위 모서리
//                drawScope.drawLine(
//                    color = COLORS[colorID][1],
//                    start = Offset(rect.left, rect.top),
//                    end = Offset(rect.left + cornerSize, rect.top),
//                    strokeWidth = strokeWidth
//                )
//                drawScope.drawLine(
//                    color = COLORS[colorID][1],
//                    start = Offset(rect.left, rect.top),
//                    end = Offset(rect.left, rect.top + cornerSize),
//                    strokeWidth = strokeWidth
//                )
//
//                // 오른쪽 위 모서리
//                drawScope.drawLine(
//                    color = COLORS[colorID][1],
//                    start = Offset(rect.right, rect.top),
//                    end = Offset(rect.right - cornerSize, rect.top),
//                    strokeWidth = strokeWidth
//                )
//                drawScope.drawLine(
//                    color = COLORS[colorID][1],
//                    start = Offset(rect.right, rect.top),
//                    end = Offset(rect.right, rect.top + cornerSize),
//                    strokeWidth = strokeWidth
//                )
//
//                // 왼쪽 아래 모서리
//                drawScope.drawLine(
//                    color = COLORS[colorID][1],
//                    start = Offset(rect.left, rect.bottom),
//                    end = Offset(rect.left + cornerSize, rect.bottom),
//                    strokeWidth = strokeWidth
//                )
//                drawScope.drawLine(
//                    color = COLORS[colorID][1],
//                    start = Offset(rect.left, rect.bottom),
//                    end = Offset(rect.left, rect.bottom - cornerSize),
//                    strokeWidth = strokeWidth
//                )
//
//                // 오른쪽 아래 모서리
//                drawScope.drawLine(
//                    color = COLORS[colorID][1],
//                    start = Offset(rect.right, rect.bottom),
//                    end = Offset(rect.right - cornerSize, rect.bottom),
//                    strokeWidth = strokeWidth
//                )
//                drawScope.drawLine(
//                    color = COLORS[colorID][1],
//                    start = Offset(rect.right, rect.bottom),
//                    end = Offset(rect.right, rect.bottom - cornerSize),
//                    strokeWidth = strokeWidth
//                )
//
//
//

//            }
//        }
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
        private const val LABEL_FORMAT = "%.2f%% confidence (index: %d)"
    }
}
