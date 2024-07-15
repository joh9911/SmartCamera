package com.example.smartcamera.composables

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BlurOff
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.NoFlash
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.utils.CameraAspectRatio


enum class IconState {
    MAIN, RATIO, FLASH, TIMER, FILTER, SETTING
}

@Composable
fun TopOptionBar(
    viewModel: MainViewModel,
    modifier: Modifier,

    ) {

    var currentIconState by remember { mutableStateOf(IconState.MAIN) }

    BackHandler(enabled = currentIconState != IconState.MAIN) {
        currentIconState = IconState.MAIN
    }

    Box(
        modifier = modifier
            .clickable { viewModel.cameraSettingManager.setISO(640) }
    ) {
        AnimatedContent(targetState = currentIconState) { state ->
            when (state) {
                IconState.MAIN -> MainIconMenu(onIconClick = { currentIconState = it })
                IconState.FLASH -> FlashIconMenu(onIconClick = {
                    currentIconState = IconState.MAIN
                })
                IconState.TIMER -> {}
                IconState.RATIO -> CameraAspectRatioSelector(viewModel)
                IconState.FILTER -> {}
                IconState.SETTING -> {}
            }
        }
    }
}

@Composable
fun FlashIconMenu(onIconClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icons = listOf(
            Icons.Outlined.FlashOn,
            Icons.Outlined.FlashOff,
            Icons.Outlined.FlashAuto,
            Icons.Outlined.NoFlash,

            )
        icons.forEach {
            Icon(
                imageVector = it,
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier
                    .size(18.dp)
                    .clickable {
                        onIconClick()
                    }

            )
        }
    }
}

@Composable
fun MainIconMenu(onIconClick: (IconState) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icons = listOf(
            Pair(Icons.Outlined.Settings, IconState.SETTING),
            Pair(Icons.Outlined.FlashOff, IconState.FLASH),
            Pair(Icons.Outlined.TimerOff, IconState.TIMER),
            Pair(Icons.Outlined.BlurOff, IconState.RATIO),
            Pair(Icons.Outlined.AutoAwesome, IconState.SETTING),
            Pair(Icons.Outlined.Diamond, IconState.FILTER),
            Pair(Icons.Outlined.AutoAwesome, IconState.SETTING),
        )
        icons.forEach {
            Icon(
                imageVector = it.first,
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onIconClick(it.second) }

            )
        }
    }
}


@Composable
fun CameraAspectRatioSelector(viewModel: MainViewModel) {

    val ratios = listOf("3:4", "9:16", "1:1", "Full")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ratios.forEach { ratio ->

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable {
                        when (ratio) {
                            "3:4" -> {viewModel.updateCameraRatio(aspectRatio = CameraAspectRatio.RATIO_4_3)}
                            "9:16" -> {viewModel.updateCameraRatio(aspectRatio = CameraAspectRatio.RATIO_16_9)}
                            "1:1" -> {}
                            "Full" -> {}
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ratio,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                


            }
        }
    }
}
