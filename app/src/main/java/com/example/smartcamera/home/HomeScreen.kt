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

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.smartcamera.composables.BottomControlBar
import com.example.smartcamera.composables.TopOptionBar
import com.example.smartcamera.home.camera.CameraView
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.utils.CameraAspectRatio
import com.example.smartcamera.utils.getFittedBoxSize

// The Home screen contains the camera view and the gallery view
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOptionsButtonClick: () -> Unit,
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
) {
    // We declare a state to control which view we're displaying: camera or gallery
    var selectedTabIndex by rememberSaveable {
        mutableStateOf(0)
    }

    // This state stores the inference time of the latest object detection process
    // to be displayed at the bottom of the screen
    var inferenceTime by rememberSaveable {
        mutableStateOf(0)
    }

    val cameraAspectRatio by viewModel.cameraRatio.collectAsState()

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
            threshold = threshold,
            maxResults = maxResults,
            delegate = delegate,
            mlModel = mlModel,
            setInferenceTime = { /* inferenceTime = it */ },
            modifier = cameraModifier

        )
        TopOptionBar(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .height(90.dp)
                .fillMaxWidth()
        )

        BottomControlBar(
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }



}







