package com.example.smartcamera.ui.viewmodel

import CameraSettingsManager
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.example.smartcamera.objectdetector.ObjectGraphic
import com.example.smartcamera.objectdetector.PoseLandmarkerHelper
import com.example.smartcamera.utils.BodyRatioCalculator
import com.example.smartcamera.utils.CameraAspectRatio
import com.example.smartcamera.utils.CameraFrameConfig
import com.example.smartcamera.utils.GuideMessage
import com.example.smartcamera.utils.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class ObjectFocusedOnState(
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val isImageFlipped: Boolean = false,
    val scaleFactor: Float = 1.0f,
    val postScaleWidthOffset: Float = 0f,
    val postScaleHeightOffset: Float = 0f,
    val needUpdateTransformation: Boolean = true,
    val graphics: List<ObjectGraphic> = emptyList(),
    val clickedPoint: Offset? = null,
    val trackedTrackingIds: Set<Int> = emptySet(),
    val isGuideModeOn: Boolean = false,
) {
    fun translateX(x: Float): Float {
        return if (isImageFlipped) {
            scaleFactor * (imageWidth - x) - postScaleWidthOffset
        } else {
            scaleFactor * x - postScaleWidthOffset
        }
    }

    fun translateY(y: Float): Float {
        return scaleFactor * y - postScaleHeightOffset
    }

    fun scale(imagePixel: Float): Float {
        return imagePixel * scaleFactor
    }
}

data class CameraSettings(
    val iso: Int = 100,
    val shutterSpeed: Long = 1_000_000_000 / 60, // in nanoseconds
    val exposureCompensation: Int = 0,
    val focusDistance: Float = 0.0f,
    val whiteBalance: Int = 0,
    val autoExposure: Boolean = true,
)

class MainViewModel : ViewModel() {
    private val _pitch = MutableStateFlow(0f)
    private val _roll = MutableStateFlow(0f)

    val pitch = _pitch.asStateFlow()
    val roll = _roll.asStateFlow()

    fun updateSensorValue(pitch: Float, roll: Float) {
        _pitch.value = pitch
        _roll.value = roll
    }

    private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    val lensFacing = _lensFacing.asStateFlow()

    private val _cameraRatio = MutableStateFlow(CameraAspectRatio.RATIO_4_3)
    val cameraRatio = _cameraRatio.asStateFlow()

    private val _cameraFrameConfig = MutableStateFlow(
        CameraFrameConfig(
            frameWidth = 3,
            frameHeight = 4
        )
    )
    val cameraFrameConfig = _cameraFrameConfig.asStateFlow()

    private val _graphicOverlayState = MutableStateFlow(ObjectFocusedOnState())
    val graphicOverlayState = _graphicOverlayState.asStateFlow()

    private val _cameraControl = MutableStateFlow<CameraControl?>(null)
    val cameraControl = _cameraControl.asStateFlow()

    fun setCameraControl(cameraControl: CameraControl) {
        _cameraControl.value = cameraControl
    }

    private val _zoomRatio = MutableStateFlow(1f)
    val zoomRatio: StateFlow<Float> = _zoomRatio

    fun setZoom(scale: Float) {
        _zoomRatio.value = scale
        cameraControl.value?.setZoomRatio(scale)
    }

    private val _focusPoint = MutableStateFlow(Pair(0f, 0f))
    val focusPoint = _focusPoint.asStateFlow()

    fun setFocusPoint(x: Float, y: Float) {
        _focusPoint.value = Pair(x, y)
    }

    private val _exposureValue = MutableStateFlow(0f)
    val exposureValue: StateFlow<Float> get() = _exposureValue

    fun setExposure(value: Float) {
        _exposureValue.value = value.coerceIn(-1f, 1f) // 예: -1부터 1까지의 노출 범위
    }

    enum class GuideState {
        Idle,
        ObjectSelected,
        PositionGuide,
        GuideComplete
    }

    var guideState = GuideState.Idle

    private val _guideMessage = MutableStateFlow<GuideMessage>(GuideMessage.EMPTY)
    val guideMessage: StateFlow<GuideMessage> = _guideMessage.asStateFlow()

    private val _guideMessageActive = MutableStateFlow(false)
    val guideMessageActive = _guideMessageActive.asStateFlow()

    fun setGuideMessageActive(boolean: Boolean){
        _guideMessageActive.value = boolean
    }

    fun setGuideMessage(message: GuideMessage) {
        _guideMessage.value = message
    }


    private val _camera2CameraControl = MutableStateFlow<Camera2CameraControl?>(null)
    val camera2CameraControl = _camera2CameraControl.asStateFlow()

    val cameraSettingManager = CameraSettingsManager(this@MainViewModel)


    @OptIn(ExperimentalCamera2Interop::class)
    fun setCamera2CameraControl(camera2CameraControl: Camera2CameraControl) {
        _camera2CameraControl.value = camera2CameraControl
    }

    private val _iso = MutableStateFlow(100)
    val iso: StateFlow<Int> = _iso.asStateFlow()

    private val _shutterSpeed = MutableStateFlow(1L)
    val shutterSpeed: StateFlow<Long> = _shutterSpeed.asStateFlow()

    private val _ev = MutableStateFlow(0)
    val ev: StateFlow<Int> = _ev.asStateFlow()

    private val _focus = MutableStateFlow(1f)
    val focus: StateFlow<Float> = _focus.asStateFlow()

    private val _wb = MutableStateFlow(5000)
    val wb: StateFlow<Int> = _wb.asStateFlow()

    fun updateISO(value: Int) {
        _iso.value = value
    }

    fun updateShutterSpeed(value: Long) {
        _shutterSpeed.value = value
    }

    fun updateEV(value: Int) {
        _ev.value = value
    }

    fun updateFocus(value: Float) {
        _focus.value = value
    }

    fun updateWB(value: Int) {
        _wb.value = value
    }

    private val _cameraMode = MutableStateFlow(0)
    val cameraMode = _cameraMode.asStateFlow()

    fun updateCameraMode(index: Int) {
        _cameraMode.value = index
    }


    fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
        _graphicOverlayState.update { currentState ->
            currentState.copy(
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                isImageFlipped = isFlipped,
                needUpdateTransformation = true
            )
        }
    }

    fun addGraphic(graphic: ObjectGraphic) {
        _graphicOverlayState.update { currentState ->
            currentState.copy(graphics = currentState.graphics + graphic)
        }
    }

    fun clearGraphics() {
        _graphicOverlayState.update { currentState ->
            currentState.copy(graphics = emptyList())
        }
    }

    fun setGuideMode(isGuideModeOn: Boolean) {
        _graphicOverlayState.update { currentState ->
            currentState.copy(isGuideModeOn = isGuideModeOn)
        }
    }

    private var lastTrackedId: Int? = null
    private var lastTrackedTime: Long = 0

    // Tracking ID를 갱신하는 함수
    fun updateTrackingId(trackingId: Int) {
        lastTrackedId = trackingId
        lastTrackedTime = System.currentTimeMillis()
    }

    // Tracking ID가 갱신되지 않았는지 확인하는 함수
    fun isTrackingIdStale(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastTrackedTime) > 500 // 1초 이상 갱신되지 않으면 stale로 간주
    }

    fun toggleTrackedTrackingId(trackingId: Int) {
        Log.d(Tag.TAG,"토글 아이디")
        _graphicOverlayState.update { currentState ->
            val updatedTrackedIds = if (currentState.trackedTrackingIds.contains(trackingId)) {
                guideState = GuideState.Idle

                setGuideMessageActive(false)
                currentState.trackedTrackingIds - trackingId
            } else {
                guideState = GuideState.ObjectSelected
                setGuideMessageActive(true)
                currentState.trackedTrackingIds + trackingId
            }
            currentState.copy(
                trackedTrackingIds = updatedTrackedIds,
                clickedPoint = null  // trackingId가 변경되면 clickedPoint를 초기화
            )
        }
    }


    fun clearTrackedTrackingIds() {
        _graphicOverlayState.update { currentState ->
            currentState.copy(trackedTrackingIds = emptySet())
        }
    }

    fun updateTransformationIfNeeded(viewWidth: Int, viewHeight: Int) {
        _graphicOverlayState.update { currentState ->
            if (!currentState.needUpdateTransformation || currentState.imageWidth <= 0 || currentState.imageHeight <= 0) {
                return@update currentState
            }

            val viewAspectRatio = viewWidth.toFloat() / viewHeight
            val imageAspectRatio = currentState.imageWidth.toFloat() / currentState.imageHeight
            var scaleFactor = currentState.scaleFactor
            var postScaleWidthOffset = 0f
            var postScaleHeightOffset = 0f

            if (viewAspectRatio > imageAspectRatio) {
                scaleFactor = viewWidth.toFloat() / currentState.imageWidth
                postScaleHeightOffset = (viewWidth.toFloat() / imageAspectRatio - viewHeight) / 2
            } else {
                scaleFactor = viewHeight.toFloat() / currentState.imageHeight
                postScaleWidthOffset = (viewHeight.toFloat() * imageAspectRatio - viewWidth) / 2
            }

            currentState.copy(
                scaleFactor = scaleFactor,
                postScaleWidthOffset = postScaleWidthOffset,
                postScaleHeightOffset = postScaleHeightOffset,
                needUpdateTransformation = false
            )
        }
    }

    fun updateClickedPoint(point: Offset?) {
        _graphicOverlayState.update { currentState ->
            currentState.copy(clickedPoint = point)
        }
        point?.let { handleObjectClick(it) }
    }

    fun handleObjectClick(clickedPoint: Offset) {
        val currentState = _graphicOverlayState.value
        val clickedObjects = currentState.graphics.filterIsInstance<ObjectGraphic>()
            .filter { it.handleClick(currentState, clickedPoint) }

        if (clickedObjects.isNotEmpty()) {
            val smallestObject = clickedObjects.minByOrNull { graphic ->
                val objRect = graphic.detectedObject.boundingBox
                objRect.width() * objRect.height()
            }
            smallestObject?.let {
                toggleTrackedTrackingId(it.detectedObject.trackingId!!)
            }
        }

        // 클릭 포인트 초기화
        updateClickedPoint(null)
    }

    private val _isGridVisible = MutableStateFlow(false)

    val isGridVisible = _isGridVisible.asStateFlow()


    private val bodyRatioCalculator = BodyRatioCalculator()

    private val _bodyRatio = MutableStateFlow(0f)
    val bodyRatio: StateFlow<Float> = _bodyRatio

    private val _isIdealRatio = MutableStateFlow(false)
    val isIdealRatio: StateFlow<Boolean> = _isIdealRatio

    fun updateBodyMeasurements(shoulderToHip: Float, hipToFoot: Float) {
        if (shoulderToHip != 0f) {
            val newRatio = hipToFoot / shoulderToHip
            val stableRatio = bodyRatioCalculator.updateRatio(newRatio)
            _bodyRatio.value = stableRatio
            _isIdealRatio.value = bodyRatioCalculator.isIdealRatio()
        }
    }

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri

    fun setCapturedImageUri(uri: Uri?) {
        _capturedImageUri.value = uri
    }

    fun updateCameraDirection() {
        if (lensFacing.value == CameraSelector.LENS_FACING_BACK)
            _lensFacing.value = CameraSelector.LENS_FACING_FRONT
        else
            _lensFacing.value = CameraSelector.LENS_FACING_BACK
    }

    fun changeIsGridVisible() {
        _isGridVisible.value = !isGridVisible.value
    }

    fun updateCameraFrameConfig(frameWidth: Int, frameHeight: Int) {
        _cameraFrameConfig.value = CameraFrameConfig(frameWidth, frameHeight)
    }

    fun updateCameraRatio(aspectRatio: CameraAspectRatio) {

        _cameraRatio.value = aspectRatio
        when (aspectRatio) {
            CameraAspectRatio.RATIO_4_3 -> {
                updateCameraFrameConfig(3, 4)
            }

            CameraAspectRatio.RATIO_16_9 -> {
                updateCameraFrameConfig(9, 16)
            }

            CameraAspectRatio.RATIO_1_1 -> {}
            CameraAspectRatio.FULL_SCREEN -> {}
        }
    }

    private val _poseLandmarkResultBundle = MutableStateFlow<PoseLandmarkerHelper.ResultBundle?>(null)
    val poseLandmarkResultBundle = _poseLandmarkResultBundle.asStateFlow()

    fun updatePoseLandmarkResultBundle(bundle: PoseLandmarkerHelper.ResultBundle?) {
        _poseLandmarkResultBundle.value = bundle
    }




}