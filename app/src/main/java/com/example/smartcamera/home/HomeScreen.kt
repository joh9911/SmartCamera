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
package com.example.smartcamera.home

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.smartcamera.composables.BottomControlBar
import com.example.smartcamera.composables.TopOptionBar
import com.example.smartcamera.home.camera.CameraView
import com.example.smartcamera.home.camera.ImageAnalyzer
import com.example.smartcamera.objectdetector.MLObjectDetectorHelper
import com.example.smartcamera.objectdetector.MLObjectDetectorListener
import com.example.smartcamera.objectdetector.ObjectGraphic
import com.example.smartcamera.objectdetector.PoseLandmarkerHelper
import com.example.smartcamera.objectdetector.PoseLandmarkerListener
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.utils.CameraAspectRatio
import com.example.smartcamera.utils.Tag
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.Executors

// The Home screen contains the camera view and the gallery view
@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOptionsButtonClick: () -> Unit,
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
    onCaptureClick: () -> Unit
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var customImageAnalyzer: ImageAnalyzer? by remember { mutableStateOf(null) }


    val cameraAspectRatio by viewModel.cameraRatio.collectAsState()
    val cameraRatio by viewModel.cameraRatio.collectAsState()
    val lensFacing by viewModel.lensFacing.collectAsState()

    val initAnalyzer = remember {
        {
            val mlObjectDetectorHelper =
                MLObjectDetectorHelper(objectDetectorListener = MLObjectDetectorListener(
                    onErrorCallback = { _, _ ->
                    },
                    onResultsCallback = { detectedObjects, width, height ->
                        viewModel.clearGraphics()
                        detectedObjects.forEach { detectedObject ->
                            viewModel.addGraphic(ObjectGraphic(toggleTrackedTrackingId = {
                                viewModel.toggleTrackedTrackingId(it)
                            }, setGuideModeOn = {
                                Log.d("detectedObject", "$it")
                                viewModel.setGuideMode(it)
                            }, detectedObject = detectedObject, viewModel = viewModel
                            )
                            )
                        }
                    }

                ))


            val poseLandmarkerHelper = PoseLandmarkerHelper(
                runningMode = RunningMode.LIVE_STREAM,
                context = context,
                poseLandmarkerHelperListener = PoseLandmarkerListener(onErrorCallback = { errorMsg, errorCode ->
                    Log.d(Tag.TAG, "poseLandmarkerHelper error $errorMsg")
                }, onResultsCallback = {
                    viewModel.updatePoseLandmarkResultBundle(it)

                })
            )

            customImageAnalyzer = ImageAnalyzer(
                viewModel, lensFacing, mlObjectDetectorHelper, poseLandmarkerHelper
            )
        }
    }
    LaunchedEffect(key1 = Unit) {
        initAnalyzer()
    }

    val takePicture: () -> Unit = remember {
        {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }

            val outputOptions = ImageCapture.OutputFileOptions.Builder(
                context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            ).build()

            imageCapture?.takePicture(outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(Tag.TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        output.savedUri?.let { uri ->
                            viewModel.setCapturedImageUri(uri)
                            onCaptureClick()
                        }
                    }
                })
        }

    }

    val bindCamera = remember {
        {
                cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
                previewView: PreviewView,
                lensFacing: Int,
                context: Context,
                setImageCapture: (ImageCapture) -> Unit,
            ->
            val executor = ContextCompat.getMainExecutor(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val previewBuilder = Preview.Builder()
                val camera2Interop = Camera2Interop.Extender(previewBuilder)
                camera2Interop.setSessionCaptureCallback(object :
                    CameraCaptureSession.CaptureCallback() {
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
                })
                val preview = previewBuilder.build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }


                // We specify what phone camera to use. In our case it's the back camera
                val cameraSelector = CameraSelector.Builder().requireLensFacing(
                    lensFacing
                ).build()

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
                }.build()


                val backgroundExecutor = Executors.newSingleThreadExecutor()

                backgroundExecutor.execute {


                    customImageAnalyzer?.let {
                        imageAnalyzer.setAnalyzer(
                            backgroundExecutor, it
                        )
                    }

                }


                cameraProvider.unbindAll()

                val imageCaptureUseCase = ImageCapture.Builder().build()
                setImageCapture(imageCaptureUseCase)

                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, imageAnalyzer, preview, imageCaptureUseCase
                )
                viewModel.setCameraControl(camera.cameraControl)


                val camera2CameraControl = Camera2CameraControl.from(camera.cameraControl)
                viewModel.setCamera2CameraControl(camera2CameraControl)


                camera2CameraControl.setCaptureRequestOptions(
                    CaptureRequestOptions.Builder().setCaptureRequestOption(
                        CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON
                    ).build()
                )


            }, executor)
        }
    }

    LaunchedEffect(key1 = lensFacing, key2 = cameraRatio) {
        bindCamera(
            cameraProviderFuture, previewView, lensFacing, context

        ) { imageCapture = it }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {

        val topBarHeight = 70.dp
        val screenHeight = maxHeight.value
        val screenWidth = maxWidth.value

        val cameraModifier = Modifier



        CameraView(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            viewModel = viewModel,
            modifier = cameraModifier,
            lifecycleOwner = lifecycleOwner,
            context = context,
            cameraProviderFuture = cameraProviderFuture,
            previewView = previewView,
        )
        TopOptionBar(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .height(90.dp)
                .fillMaxWidth()
        )

        BottomControlBar(viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            onCaptureClick = takePicture

        )
    }


}







