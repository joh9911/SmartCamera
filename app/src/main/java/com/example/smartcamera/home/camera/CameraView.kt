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
import android.util.Log
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.FocusMeteringAction
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.example.smartcamera.composables.ImageSegmenterOverlay
import com.example.smartcamera.composables.overlay.FaceLandmarkOverlay
import com.example.smartcamera.composables.overlay.FocusRing
import com.example.smartcamera.composables.overlay.GuideOverlay
import com.example.smartcamera.composables.overlay.MessageOverlay
import com.example.smartcamera.composables.overlay.ObjectFocusedOnOverlay
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.utils.Tag
import com.example.smartcamera.utils.getFittedBoxSize
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectionResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.nio.ByteBuffer


@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraView(
    screenWidth: Float,
    screenHeight: Float,
    viewModel: MainViewModel,
    modifier: Modifier,
    lifecycleOwner: LifecycleOwner,
    context: Context,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    previewView: PreviewView,

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

    val cameraConfig by viewModel.cameraFrameConfig.collectAsState()
    val cameraMode by viewModel.cameraMode.collectAsState()
    val isGirdVisible by viewModel.isGridVisible.collectAsState()
    val focusPoint by viewModel.focusPoint.collectAsState()
    val guideMessage by viewModel.guideMessage.collectAsState()
    val guideMessageActive by viewModel.guideMessageActive.collectAsState()



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
        GuideOverlay(previewWidth = cameraPreviewSize.width, previewHeight = cameraPreviewSize.height)
        if (cameraMode == 0) {
            PoseLandmarkerOverlay(
                viewModel = viewModel,
            )
            ObjectFocusedOnOverlay(viewModel = viewModel)

            if (guideMessageActive) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize(),
//                    contentAlignment = Alignment.TopCenter
//                ) {
//                    MessageOverlay(
//                        message = guideMessage,
//                        isVisible = guideMessageActive,
//                        onDismiss = { isGuideMessageVisible = false }
//                    )
//                }
            }

            imageSegmenterResults?.let {
                ImageSegmenterOverlay(
                    byteBuffer = it,
                    outputWidth = outputWidth,
                    outputHeight = outputHeight
                )
            }
            faceLandmarkerResults?.let {
                FaceLandmarkOverlay(
                    faceLandmarkerResults = it,
                    imageWidth = faceLandmarkerFrameWidth,
                    imageHeight = faceLandmarkerFrameHeight
                )
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





