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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartcamera.composables.MediaPipeBanner
import com.example.smartcamera.home.camera.CameraView
import com.example.smartcamera.home.gallery.GalleryView
import com.google.mediapipe.examples.objectdetection.composables.TabsTopBar

// The Home screen contains the camera view and the gallery view
@Composable
fun HomeScreen(
    onOptionsButtonClick: () -> Unit,
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
    isGridVisible: Boolean,
    cameraDirection: String,
    changeCameraDirection: () -> Unit
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

    Column {
        // MediaPipe banner showing an options button to navigate to Options screen with
        MediaPipeBanner(
            onOptionsButtonClick = onOptionsButtonClick,
        )
        // The tabs at the top to switch between camera and gallery views
        TabsTopBar(
            selectedTabIndex = selectedTabIndex,
            setSelectedTabIndex = {
                selectedTabIndex = it
                inferenceTime = 0
            }
        )
        // Here we display the camera view or the gallery view based on the selected tab, both
        // of which need to be provided with the object detector options as well as a function
        // to update the inference value when running an object detection process
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (selectedTabIndex == 0) {
                CameraView(
                    threshold = threshold,
                    maxResults = maxResults,
                    delegate = delegate,
                    mlModel = mlModel,
                    setInferenceTime = { inferenceTime = it },
                    isGridVisible = isGridVisible,
                    cameraDirection = cameraDirection
                )
            } else {
                GalleryView(
                    threshold = threshold,
                    maxResults = maxResults,
                    delegate = delegate,
                    mlModel = mlModel,
                    setInferenceTime = { inferenceTime = it },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Transparent),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 투명한 회색 배경의 버튼 스타일
            val buttonColors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f))

            // 이미지 파일 확인 버튼
            Button(
                onClick = {},
                colors = buttonColors
            ) {
                Text("Image",
                    modifier = Modifier.size(8.dp))
            }

            // 사진 찍기 버튼
            Button(
                onClick = {},
                colors = buttonColors
            ) {
                Text("Take Photo",
                    modifier = Modifier.size(8.dp))
            }

            // 카메라 전환 버튼
            IconButton(
                onClick = {changeCameraDirection()},
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Gray.copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "flip camera direction")
            }
        }
        Box(
            modifier = Modifier
                .padding(10.dp),
        ) {
            Text(text = "Inference Time: $inferenceTime ms")
        }
    }
}




