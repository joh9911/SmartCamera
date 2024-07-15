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
package com.example.smartcamera

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartcamera.home.HomeScreen
import com.example.smartcamera.home.ImagePreviewScreen
import com.example.smartcamera.objectdetector.ObjectDetectorHelper
import com.example.smartcamera.ui.theme.SmartCameraTheme
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.utils.BalanceSensorManager
import com.google.accompanist.systemuicontroller.rememberSystemUiController

//Entry point of our example app
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var sensorManager: BalanceSensorManager

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SmartCamera) // 스플래시 스크린 이후 앱 테마 설정

        super.onCreate(savedInstanceState)
        viewModel = viewModels<MainViewModel>().value
        sensorManager = BalanceSensorManager(this) { pitch, roll ->
            viewModel.updateSensorValue(pitch, roll)
        }

        enableEdgeToEdge()

        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }


        setContent {
            SmartCameraApp(viewModel)
        }
    }

}

// Root component of our app components tree
@Composable
fun SmartCameraApp(
    viewModel: MainViewModel,
) {
    val systemUiController = rememberSystemUiController()

    // 상태바를 투명하게 설정
    systemUiController.setStatusBarColor(
        color = Color.Transparent, // 완전 투명
        darkIcons = true // 상태바 아이콘을 어둡게 설정
    )

    val threshold by rememberSaveable {
        mutableStateOf(0.4f)
    }
    val maxResults by rememberSaveable {
        mutableStateOf(5)
    }
    val delegate by rememberSaveable {
        mutableStateOf(ObjectDetectorHelper.DELEGATE_CPU)
    }
    val mlModel by rememberSaveable {
        mutableStateOf(ObjectDetectorHelper.MODEL_EFFICIENTDETV0)
    }


    SmartCameraTheme(darkTheme = false) {

        Surface(modifier = Modifier.fillMaxSize()) {
            // Here we handle navigation between Home screen and Options screen
            // Nothing too fancy, we only have two screens.

            // We define a controller first and provide it to NavHost
            // We will later use it to navigate between screens
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "Home",
            ) {
                // Here we associate a route name with each screen
                // We also provide a callback function to each screen
                // to navigate to the other one
                composable(route = "Home",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }) {

                    HomeScreen(
                        viewModel = viewModel,
                        onOptionsButtonClick = {
                            navController.navigate("Options")
                        },
                        threshold = threshold,
                        maxResults = maxResults,
                        delegate = delegate,
                        mlModel = mlModel,
                        onCaptureClick = {navController.navigate("Preview")}
                    )
                }
                composable(route = "Preview",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }){
                    ImagePreviewScreen(viewModel = viewModel, onBackPressed = {
                        navController.popBackStack()
                    })
                }
                composable(route = "Options") {
//                    OptionsScreen(
//                        onBackButtonClick = {
//                            navController.popBackStack()
//                        },
//                        threshold = threshold, setThreshold = { threshold = it },
//                        maxResults = maxResults, setMaxResults = { maxResults = it },
//                        delegate = delegate, setDelegate = { delegate = it },
//                        mlModel = mlModel, setMlModel = { mlModel = it },
//                    )
                }
            }
        }
    }
}


