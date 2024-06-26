/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.smartcamera.home.camera

import PoseLandmarkerOverlay
import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.smartcamera.composables.ImageSegmenterOverlay
import com.example.smartcamera.composables.overlay.FaceLandmarkOverlay
import com.example.smartcamera.composables.overlay.FocusRing
import com.example.smartcamera.composables.overlay.MessageOverlay
import com.example.smartcamera.composables.overlay.ObjectDetectionOverlay
import com.example.smartcamera.composables.overlay.ObjectFocusedOnOverlay
import com.example.smartcamera.objectdetector.FaceLandmarkerHelper
import com.example.smartcamera.objectdetector.FaceLandmarkerListener
import com.example.smartcamera.objectdetector.ImageSegmenterHelper
import com.example.smartcamera.objectdetector.ImageSegmenterListener
import com.example.smartcamera.objectdetector.MLObjectDetectorHelper
import com.example.smartcamera.objectdetector.MLObjectDetectorListener
import com.example.smartcamera.objectdetector.ObjectGraphic
import com.example.smartcamera.objectdetector.PoseLandmarkerHelper
import com.example.smartcamera.objectdetector.PoseLandmarkerListener
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.utils.CameraAspectRatio
import com.example.smartcamera.utils.Tag
import com.example.smartcamera.utils.getFittedBoxSize
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectionResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.nio.ByteBuffer
import java.util.concurrent.Executors


@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraView(
    screenWidth: Float,
    screenHeight: Float,
    viewModel: MainViewModel,
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
    setInferenceTime: (newInferenceTime: Int) -> Unit,
    modifier: Modifier,
) {


    val storagePermissionState: PermissionState =
        rememberPermissionState(Manifest.permission.CAMERA)


    LaunchedEffect(key1 = Unit) {
        if (!storagePermissionState.hasPermission) {
            storagePermissionState.launchPermissionRequest()
        }

    }


    if (!storagePermissionState.hasPermission) {
        Text(text = "No Storage Permission!")
        return
    }


    var objectDetectResults by remember {
        mutableStateOf<ObjectDetectionResult?>(null)
    }
    var faceDetectResults by remember {
        mutableStateOf<FaceDetectorResult?>(null)
    }
    var faceLandmarkerResults by remember {
        mutableStateOf<FaceLandmarkerResult?>(null)
    }

    var imageSegmenterResults by remember {
        mutableStateOf<ByteBuffer?>(null)
    }
    var outputWidth by remember {
        mutableStateOf(3)
    }
    var outputHeight by remember {
        mutableStateOf(4)
    }

    var poseLandmarkerResult by remember {
        mutableStateOf<PoseLandmarkerResult?>(null)
    }
    var poseLandmarkerInputWidth by remember {
        mutableStateOf(3)
    }
    var poseLandermarkerInputHeight by remember {
        mutableStateOf(4)
    }

    var faceLandmarkerFrameWidth by remember {
        mutableStateOf(3)
    }
    var faceLandmarkerFrameHeight by remember {
        mutableStateOf(4)
    }

    var active by remember {
        mutableStateOf(true)
    }

    var isGuideMessageVisible by remember {
        mutableStateOf(true)
    }

    val cameraRatio by viewModel.cameraRatio.collectAsState()
    val cameraConfig by viewModel.cameraFrameConfig.collectAsState()
    val lensFacing by viewModel.lensFacing.collectAsState()
    val cameraMode by viewModel.cameraMode.collectAsState()
    val isGirdVisible by viewModel.isGridVisible.collectAsState()
    val focusPoint by viewModel.focusPoint.collectAsState()
    val guideMessage by viewModel.guideMessage.collectAsState()
    val guideMessageActive by viewModel.guideMessageActive.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(key1 = focusPoint) {

        if (focusPoint == Pair(0f, 0f)) return@LaunchedEffect
        Log.d("포커스 포인트", "바뀜")
        val meteringPoint = previewView.meteringPointFactory
            .createPoint(focusPoint.first, focusPoint.second)


        val action = FocusMeteringAction.Builder(meteringPoint) // default AF|AE|AWB
            .build()

        viewModel.cameraControl.value?.startFocusAndMetering(action)
// Adds listener to the ListenableFuture if you need to know the focusMetering result.

    }


    LaunchedEffect(key1 = lensFacing, key2 = cameraRatio) {
        bindCamera(
            viewModel,
            cameraProviderFuture,
            previewView,
            lensFacing,
            context,
            lifecycleOwner,
            threshold,
            maxResults,
            delegate,
            mlModel,
            { objectDetectResults = it },
            { faceDetectResults = it },
            { faceLandmarkerResults = it },
            { byteBuffer, width, height ->
                imageSegmenterResults = byteBuffer
                outputWidth = width
                outputHeight = height
            },
            { result, width, height ->
                poseLandmarkerResult = result
                poseLandmarkerInputWidth = width
                poseLandermarkerInputHeight = height
            },
            setInferenceTime,
            active,

            )
    }


    DisposableEffect(Unit) {
        onDispose {
            active = false
            cameraProviderFuture.get().unbindAll()
        }
    }

    // Next we describe the UI of this camera view.


    val cameraPreviewSize = getFittedBoxSize(
        containerSize = Size(
            width = screenWidth,
            height = screenHeight,
        ),
        boxSize = Size(
            width = cameraConfig.frameWidth.toFloat(),
            height = cameraConfig.frameHeight.toFloat()
        )
    )
    Log.d("cameraPreviewSize", "$cameraPreviewSize")
    var focusPointState by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0.5f) } // 0f부터 1f까지의 범위


    Box(
        modifier =
        modifier
            .offset(y = 70.dp)
            .width(cameraPreviewSize.width.dp)
            .height(cameraPreviewSize.height.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    Log.d(Tag.TAG, "클릭")
                    focusPointState = offset
                    viewModel.setFocusPoint(offset.x, offset.y)

                    isDragging = true
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->

                    val newZoom = (viewModel.zoomRatio.value * zoom).coerceIn(1f, 10f)
                    viewModel.setZoom(newZoom)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    if (isDragging) {

                    }
                }
            }
    ) {

        AndroidView(
            factory = { ctx ->
                previewView
            },

            modifier = Modifier.fillMaxSize(),
        )
//        if (isGirdVisible)
//            GridOverlay()

        if (cameraMode == 0) {
            PoseLandmarkerOverlay(
                poseLandmarkerResult = poseLandmarkerResult,
                imageHeight = poseLandermarkerInputHeight,
                imageWidth = poseLandmarkerInputWidth
            )
            ObjectFocusedOnOverlay(viewModel = viewModel)

            if (guideMessageActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    MessageOverlay(
                        message = guideMessage,
                        isVisible = guideMessageActive,
                        onDismiss = { isGuideMessageVisible = false }
                    )
                }
            }


            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                imageSegmenterResults?.let {
                    ImageSegmenterOverlay(
                        byteBuffer = it,
                        outputWidth = outputWidth,
                        outputHeight = outputHeight
                    )
                }

                objectDetectResults?.let {
                    ObjectDetectionOverlay(
                        objectDetectionResults = it,
                        frameWidth = cameraConfig.frameWidth,
                        frameHeight = cameraConfig.frameHeight
                    )
                }
            } else {

                faceLandmarkerResults?.let {
                    FaceLandmarkOverlay(
                        faceLandmarkerResults = it,
                        imageWidth = faceLandmarkerFrameWidth,
                        imageHeight = faceLandmarkerFrameHeight
                    )
                }


            }
        } else {
            focusPointState?.let { offset ->
                FocusRing(
                    position = offset,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }


    }
}


@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
private fun bindCamera(
    viewModel: MainViewModel,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    previewView: PreviewView,
    lensFacing: Int,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
    setObjectDetectionResult: (ObjectDetectionResult?) -> Unit,
    setFaceDetectionResult: (FaceDetectorResult) -> Unit,
    setFaceLandmarkerResult: (FaceLandmarkerResult?) -> Unit,
    setImageSegmenterResult: (ByteBuffer, Int, Int) -> Unit,
    setPoseLandmarkerResult: (PoseLandmarkerResult, Int, Int) -> Unit,
    setInferenceTime: (Int) -> Unit,
    active: Boolean,
) {
    val executor = ContextCompat.getMainExecutor(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val previewBuilder = Preview.Builder()
        val camera2Interop = Camera2Interop.Extender(previewBuilder)
        camera2Interop.setSessionCaptureCallback(
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult,
                ) {
                    val iso = result.get(CaptureResult.SENSOR_SENSITIVITY)
                    val shutterSpeed = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
                    val ev = result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION)
                    val focus = result.get(CaptureResult.LENS_FOCUS_DISTANCE)
                    val wb = result.get(CaptureResult.CONTROL_AWB_MODE)

                    iso?.let { viewModel.updateISO(it) }
                    shutterSpeed?.let { viewModel.updateShutterSpeed(it) }
                    ev?.let { viewModel.updateEV(it) }
                    focus?.let { viewModel.updateFocus(it) }
                    wb?.let { viewModel.updateWB(it) }
                }
            }
        )
        val preview = previewBuilder.build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }


        // We specify what phone camera to use. In our case it's the back camera
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(
                lensFacing
            )
            .build()

        val imageAnalyzer = ImageAnalysis.Builder().apply {
            val resolutionSelectorBuilder = ResolutionSelector.Builder().apply {
                if (viewModel.cameraRatio.value == CameraAspectRatio.RATIO_4_3) {
                    setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                } else if (viewModel.cameraRatio.value == CameraAspectRatio.RATIO_16_9) {
                    setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                }
            }
            this.setResolutionSelector(resolutionSelectorBuilder.build())
            this.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            this.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        }
            .build()


        val backgroundExecutor = Executors.newSingleThreadExecutor()

        backgroundExecutor.execute {


            val mlObjectDetectorHelper = MLObjectDetectorHelper(
                objectDetectorListener = MLObjectDetectorListener(
                    onErrorCallback = { _, _ ->
                    },
                    onResultsCallback = { detectedObjects, width, height ->
                        viewModel.clearGraphics()
                        detectedObjects.forEach { detectedObject ->
                            viewModel.addGraphic(
                                ObjectGraphic(
                                    toggleTrackedTrackingId = {
                                        viewModel.toggleTrackedTrackingId(it)
                                    },
                                    setGuideModeOn = {
                                        Log.d("detectedObject", "$it")
                                        viewModel.setGuideMode(it)
                                    },
                                    detectedObject = detectedObject,
                                    viewModel = viewModel
                                )
                            )
                        }
                    }
                )
            )

            val faceLandmarkerHelper = FaceLandmarkerHelper(
                context = context,
                faceLandmarkerHelperListener = FaceLandmarkerListener(
                    onErrorCallback = { _, _ ->
                        Log.d("onErrorCallback", "실패")
                    },
                    onResultsCallback = {

                        setFaceLandmarkerResult(it.result)
                        if (it.result.faceBlendshapes().isPresent) {
                            val faceBlendshapes = it.result.faceBlendshapes().get()
                            val sortedCategories =
                                faceBlendshapes[0].sortedByDescending { it.score() }

                            for (i in sortedCategories.indices) {
                                val name = sortedCategories[i].categoryName()
                                val score = sortedCategories[i].score()
                                val formattedValue = String.format("%.2f", score)


                            }
                        }
                    },
                    onEmptyCallback = {
                        setFaceLandmarkerResult(null)
                    }
                )
            )
            val imageSegmenterHelper = ImageSegmenterHelper(
                runningMode = RunningMode.LIVE_STREAM,
                context = context,
                imageSegmenterListener = ImageSegmenterListener(
                    onErrorCallback = { errorMsg, errorCode ->
                        Log.d(Tag.TAG, "errorMsg$errorMsg")
                    },
                    onResultsCallback = {
                        setImageSegmenterResult(it.results, it.width, it.height)
                    }
                )
            )

            val poseLandmarkerHelper = PoseLandmarkerHelper(
                runningMode = RunningMode.LIVE_STREAM,
                context = context,
                poseLandmarkerHelperListener = PoseLandmarkerListener(
                    onErrorCallback = { errorMsg, errorCode ->
                        Log.d(Tag.TAG, "poseLandmarkerHelper error $errorMsg")
                    },
                    onResultsCallback = {
                        setPoseLandmarkerResult(
                            it.results.first(),
                            it.inputImageWidth,
                            it.inputImageHeight
                        )
                    }
                )
            )

            val customImageAnalyzer =
                ImageAnalyzer(
                    viewModel,
                    lensFacing,
                    faceLandmarkerHelper,
                    mlObjectDetectorHelper,
                    imageSegmenterHelper,
                    poseLandmarkerHelper
                )


            imageAnalyzer.setAnalyzer(
                backgroundExecutor,
                customImageAnalyzer
            )
        }


        cameraProvider.unbindAll()


        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            imageAnalyzer,
            preview
        )
        viewModel.setCameraControl(camera.cameraControl)


        val camera2CameraControl = Camera2CameraControl.from(camera.cameraControl)
        viewModel.setCamera2CameraControl(camera2CameraControl)


        camera2CameraControl.setCaptureRequestOptions(
            CaptureRequestOptions.Builder()
                .setCaptureRequestOption(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                .build()
        )


    }, executor)
}




