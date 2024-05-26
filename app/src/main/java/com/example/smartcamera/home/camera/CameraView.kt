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

import GridOverlay
import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

import com.google.common.util.concurrent.ListenableFuture
import com.example.smartcamera.composables.FaceDetectionOverlay
import com.example.smartcamera.composables.ObjectDetectionOverlay
import com.google.mediapipe.examples.objectdetection.home.camera.CameraState.CAMERA_FRONT
import com.example.smartcamera.objectdetector.FaceDetectorHelper
import com.example.smartcamera.objectdetector.FaceDetectorListener
import com.example.smartcamera.objectdetector.ObjectDetectorHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.example.smartcamera.objectdetector.ObjectDetectorListener
import com.google.mediapipe.examples.objectdetection.utils.getFittedBoxSize
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectionResult
import java.util.concurrent.Executors


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraView(
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
    setInferenceTime: (newInferenceTime: Int) -> Unit,
    isGridVisible: Boolean,
    cameraDirection: String
) {

    Log.d("CameraView", "CameraView is recomposed with cameraDirection: $cameraDirection")

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

    var objectDetectionFrameHeight by remember {
        mutableStateOf(4)
    }

    var objectDetectionFrameWidth by remember {
        mutableStateOf(3)
    }

    var faceDetectionFrameHeight by remember {
        mutableStateOf(4)
    }

    var faceDetectionFrameWidth by remember {
        mutableStateOf(3)
    }

    var active by remember {
        mutableStateOf(true)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(key1 = cameraDirection) {
        bindCamera(
            cameraProviderFuture,
            previewView,
            cameraDirection,
            context,
            lifecycleOwner,
            threshold,
            maxResults,
            delegate,
            mlModel,
            { objectDetectionFrameWidth = it },
            { objectDetectionFrameHeight = it },
            { faceDetectionFrameWidth = it },
            { faceDetectionFrameHeight = it },
            { objectDetectResults = it },
            { faceDetectResults = it},
            setInferenceTime,
            active
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            active = false
            cameraProviderFuture.get().unbindAll()
        }
    }

    // Next we describe the UI of this camera view.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {

        val cameraPreviewSize = getFittedBoxSize(
            containerSize = Size(
                width = this.maxWidth.value,
                height = this.maxHeight.value,
            ),
            boxSize = Size(
                width = objectDetectionFrameWidth.toFloat(),
                height = objectDetectionFrameHeight.toFloat()
            )
        )


        Box(
            Modifier
                .width(cameraPreviewSize.width.dp)
                .height(cameraPreviewSize.height.dp),
        ) {


            AndroidView(
                factory = { ctx ->
                    previewView
                },

                modifier = Modifier.fillMaxSize(),
            )
            if (isGridVisible)
                GridOverlay()
            objectDetectResults?.let {

                ObjectDetectionOverlay(
                    objectDetectionResults = it,
                    frameWidth = objectDetectionFrameWidth,
                    frameHeight = objectDetectionFrameHeight
                )
            }
            faceDetectResults?.let {
                FaceDetectionOverlay(
                    faceDetectorResults = it,
                    frameWidth = objectDetectionFrameWidth,
                    frameHeight = objectDetectionFrameHeight
                )
            }
        }
    }
}

private fun bindCamera(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    previewView: PreviewView,
    cameraDirection: String,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
    setObjectDetectionFrameWidth: (Int) -> Unit,
    setObjectDetectionFrameHeight: (Int) -> Unit,
    setFaceDetectionFrameWidth: (Int) -> Unit,
    setFaceDetectionFrameHeight: (Int) -> Unit,
    setObjectDetectionResult: (ObjectDetectionResult) -> Unit,
    setFaceDetectionResult: (FaceDetectorResult) -> Unit,
    setInferenceTime: (Int) -> Unit,
    active: Boolean
) {
    val executor = ContextCompat.getMainExecutor(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()


        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // We specify what phone camera to use. In our case it's the back camera
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(
                if (cameraDirection == CAMERA_FRONT) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            )
            .build()


        val imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()


        val backgroundExecutor = Executors.newSingleThreadExecutor()

        backgroundExecutor.execute {


            val objectDetectorHelper =
                ObjectDetectorHelper(
                    context = context,
                    threshold = threshold,
                    currentDelegate = delegate,
                    currentModel = mlModel,
                    maxResults = maxResults,
                    // Since we're detecting objects in a live camera feed, we need
                    // to have a way to listen for the results
                    objectDetectorListener = ObjectDetectorListener(
                        onErrorCallback = { _, _ -> },
                        onResultsCallback = {

                            setObjectDetectionFrameWidth(it.inputImageWidth)
                            setObjectDetectionFrameHeight(it.inputImageHeight)


                            if (active) {
                                setObjectDetectionResult(it.results.first())
                                setInferenceTime(it.inferenceTime.toInt())
                            }
                        }
                    ),
                    runningMode = RunningMode.LIVE_STREAM
                )
            val faceDetectorHelper = FaceDetectorHelper(
                threshold = threshold,
                currentDelegate = delegate,
                context = context,
                faceDetectorListener = FaceDetectorListener(
                    onErrorCallback = { _, _ -> },
                    onResultsCallback = {
                        setFaceDetectionFrameWidth(it.inputImageWidth)
                        setFaceDetectionFrameHeight(it.inputImageHeight)

                        if (active){
                            setFaceDetectionResult(it.results.first())
                        }
                    }
                )
            )

            val customImageAnalyzer = ImageAnalyzer(objectDetectorHelper)


            imageAnalyzer.setAnalyzer(
                backgroundExecutor,
                customImageAnalyzer
            )
        }


        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            imageAnalyzer,
            preview
        )
    }, executor)
}



