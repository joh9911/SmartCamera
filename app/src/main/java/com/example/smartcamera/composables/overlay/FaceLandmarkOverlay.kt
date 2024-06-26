package com.example.smartcamera.composables.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.graphics.Color

@Composable
fun FaceLandmarkOverlay(
    faceLandmarkerResults: FaceLandmarkerResult?,
    imageWidth: Int,
    imageHeight: Int,
    runningMode: RunningMode = RunningMode.IMAGE
) {
    val context = LocalContext.current

    var scaleFactor by remember { mutableStateOf(1f) }

    val linePaint = remember {
        Paint().apply {
            color = Color.Gray
            strokeWidth = 2.toFloat()
            style = androidx.compose.ui.graphics.PaintingStyle.Stroke
            strokeCap = StrokeCap.Round
            strokeJoin = StrokeJoin.Round
            pathEffect = PathEffect.cornerPathEffect(2.toFloat())
        }
    }

    val pointPaint = remember {
        Paint().apply {
            color = Color.Yellow
            strokeWidth = 4.toFloat()
            style = androidx.compose.ui.graphics.PaintingStyle.Fill
        }
    }

    if (faceLandmarkerResults != null && faceLandmarkerResults.faceLandmarks().isNotEmpty()) {
        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                min(LocalContext.current.resources.displayMetrics.widthPixels * 1f / imageWidth,
                    LocalContext.current.resources.displayMetrics.heightPixels * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(LocalContext.current.resources.displayMetrics.widthPixels * 1f / imageWidth,
                    LocalContext.current.resources.displayMetrics.heightPixels * 1f / imageHeight)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            faceLandmarkerResults?.let { results ->
                results.faceLandmarks().forEach { landmark ->
                    landmark.forEach { normalizedLandmark ->
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(
                                normalizedLandmark.x() * imageWidth * scaleFactor,
                                normalizedLandmark.y() * imageHeight * scaleFactor
                            ),
                        )
                    }
                }

                FaceLandmarker.FACE_LANDMARKS_CONNECTORS.forEach {
                    val startLandmark = results.faceLandmarks()[0][it.start()]
                    val endLandmark = results.faceLandmarks()[0][it.end()]
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(
                            startLandmark.x() * imageWidth * scaleFactor,
                            startLandmark.y() * imageHeight * scaleFactor
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            endLandmark.x() * imageWidth * scaleFactor,
                            endLandmark.y() * imageHeight * scaleFactor
                        ),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}
