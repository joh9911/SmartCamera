import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min

@Composable
fun PoseLandmarkerOverlay(
    poseLandmarkerResult: PoseLandmarkerResult?,
    imageHeight: Int,
    imageWidth: Int,
    runningMode: RunningMode = RunningMode.IMAGE,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var scaleFactor by remember { mutableStateOf(1f) }

    LaunchedEffect(imageHeight, imageWidth, runningMode) {
        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                min(
                    context.resources.displayMetrics.widthPixels * 1f / imageWidth,
                    context.resources.displayMetrics.heightPixels * 1f / imageHeight
                )
            }
            RunningMode.LIVE_STREAM -> {
                max(
                    context.resources.displayMetrics.widthPixels * 1f / imageWidth,
                    context.resources.displayMetrics.heightPixels * 1f / imageHeight
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        poseLandmarkerResult?.let { result ->
            drawPoseLandmarks(result, imageWidth, imageHeight, scaleFactor)
        }
    }
}

private fun DrawScope.drawPoseLandmarks(
    poseLandmarkerResult: PoseLandmarkerResult,
    imageWidth: Int,
    imageHeight: Int,
    scaleFactor: Float
) {
    val pointColor = Color.Yellow
    val lineColor = Color(0xFF00796B) // Replace with your desired color

    for (landmark in poseLandmarkerResult.landmarks()) {
        for (normalizedLandmark in landmark) {
            val point = Offset(
                normalizedLandmark.x() * imageWidth * scaleFactor,
                normalizedLandmark.y() * imageHeight * scaleFactor
            )
            drawCircle(
                color = pointColor,
                radius = LANDMARK_STROKE_WIDTH / 2,
                center = point
            )
        }

        PoseLandmarker.POSE_LANDMARKS.forEach { connection ->
            val start = landmark[connection.start()]
            val end = landmark[connection.end()]

            drawLine(
                color = lineColor,
                start = Offset(
                    start.x() * imageWidth * scaleFactor,
                    start.y() * imageHeight * scaleFactor
                ),
                end = Offset(
                    end.x() * imageWidth * scaleFactor,
                    end.y() * imageHeight * scaleFactor
                ),
                strokeWidth = LANDMARK_STROKE_WIDTH
            )
        }
    }
}

private const val LANDMARK_STROKE_WIDTH = 12f