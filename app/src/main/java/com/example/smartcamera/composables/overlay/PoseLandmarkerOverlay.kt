import android.util.Log
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
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun PoseLandmarkerOverlay(
    viewModel: MainViewModel,

    runningMode: RunningMode = RunningMode.IMAGE,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var scaleFactor by remember { mutableStateOf(1f) }
    val resultBundle = viewModel.poseLandmarkResultBundle.collectAsState()
    val imageHeight = resultBundle.value?.inputImageHeight ?: 3
    val imageWidth = resultBundle.value?.inputImageWidth ?: 4
    val result = resultBundle.value?.results?.first()

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
        result?.let { result ->
            drawPoseLandmarks(viewModel, result, imageWidth, imageHeight, scaleFactor)
        }
    }
}

private fun DrawScope.drawPoseLandmarks(
    viewModel: MainViewModel,
    poseLandmarkerResult: PoseLandmarkerResult,
    imageWidth: Int,
    imageHeight: Int,
    scaleFactor: Float
) {
    val pointColor = Color.Yellow
    val lineColor = Color(0xFF00796B)
    val visibilityThreshold = 0.5f // 조정 가능한 임계값

    

    poseLandmarkerResult.landmarks().forEachIndexed { personIndex, personLandmarks ->
        val visibleLandmarks = personLandmarks.filterIndexed { index, normalizedLandmark ->
            isLandmarkVisible(normalizedLandmark, imageWidth, imageHeight) &&
                    (poseLandmarkerResult.landmarks()[personIndex][index].visibility()?.get()
                        ?: 0f) > visibilityThreshold
        }

        visibleLandmarks.forEach { normalizedLandmark ->
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
            val start = personLandmarks[connection.start()]
            val end = personLandmarks[connection.end()]

            if (isLandmarkVisible(start, imageWidth, imageHeight) &&
                isLandmarkVisible(end, imageWidth, imageHeight) &&
                (poseLandmarkerResult.landmarks()[personIndex][connection.start()].visibility()
                    ?.get() ?: 0f) > visibilityThreshold &&
                (poseLandmarkerResult.landmarks()[personIndex][connection.end()].visibility()?.get()
                    ?: 0f) > visibilityThreshold
            ) {
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

        val leftShoulder = personLandmarks[11]
        val rightShoulder = personLandmarks[12]
        val leftHip = personLandmarks[23]
        val rightHip = personLandmarks[24]
        val leftAnkle = personLandmarks[27]
        val rightAnkle = personLandmarks[28]

        if (isLandmarkVisible(leftShoulder, imageWidth, imageHeight) &&
            isLandmarkVisible(rightShoulder, imageWidth, imageHeight) &&
            isLandmarkVisible(leftHip, imageWidth, imageHeight) &&
            isLandmarkVisible(rightHip, imageWidth, imageHeight) &&
            isLandmarkVisible(leftAnkle, imageWidth, imageHeight) &&
            isLandmarkVisible(rightAnkle, imageWidth, imageHeight)
        ) {
            val shoulderMidPoint = Offset(
                (leftShoulder.x() + rightShoulder.x()) / 2,
                (leftShoulder.y() + rightShoulder.y()) / 2
            )
            val hipMidPoint = Offset(
                (leftHip.x() + rightHip.x()) / 2,
                (leftHip.y() + rightHip.y()) / 2
            )
            val ankleMidPoint = Offset(
                (leftAnkle.x() + rightAnkle.x()) / 2,
                (leftAnkle.y() + rightAnkle.y()) / 2
            )

            val shoulderToHipLength = calculateDistance(shoulderMidPoint, hipMidPoint)
            val hipToAnkleLength = calculateDistance(hipMidPoint, ankleMidPoint)

            viewModel.updateBodyMeasurements(shoulderToHipLength, hipToAnkleLength)


        }
    }
}

private fun calculateDistance(point1: Offset, point2: Offset): Float {
    val dx = point2.x - point1.x
    val dy = point2.y - point1.y
    return sqrt(dx * dx + dy * dy)
}

private fun isLandmarkVisible(landmark: NormalizedLandmark, imageWidth: Int, imageHeight: Int): Boolean {
    return landmark.x() in 0f..1f && landmark.y() in 0f..1f
}

private const val LANDMARK_STROKE_WIDTH = 12f