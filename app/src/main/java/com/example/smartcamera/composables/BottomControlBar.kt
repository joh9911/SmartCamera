package com.example.smartcamera.composables

import androidx.camera.core.CameraSelector
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.utils.CameraAspectRatio

@Composable
fun BottomControlBar(
    viewModel: MainViewModel,
    modifier: Modifier,
    onCaptureClick: () -> Unit

) {

    val ratio by viewModel.bodyRatio.collectAsState()

    val selectedMode by viewModel.cameraMode.collectAsState()

    Column(modifier = modifier) {
        if (selectedMode == 1){
            SettingsBar(viewModel = viewModel)
        }
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(13.dp)
            )
            Column {

                Text("Body Ratio: ${String.format("%.2f", ratio)}", color = Color.White)
            }



            CameraModeSelector(viewModel, selectedMode)

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 투명한 회색 배경의 버튼 스타일
                val buttonColors =
                    ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f))

                // 이미지 파일 확인 버튼
                Button(
                    onClick = {
                        val cameraRatio = viewModel.cameraRatio.value
                        if (cameraRatio == CameraAspectRatio.RATIO_4_3) {
                            viewModel.updateCameraRatio(CameraAspectRatio.RATIO_16_9)
                        } else {
                            viewModel.updateCameraRatio(CameraAspectRatio.RATIO_4_3)
                        }
                    },
                    colors = buttonColors,
                    modifier = Modifier.size(48.dp) // 적절한 크기로 조정
                ) {
                    Text("Image")
                }

                // 사진 찍기 버튼
                CameraShutterButton(
                    onCapture = {
                        onCaptureClick()
                    }
                )

                // 카메라 전환 버튼
                IconButton(
                    onClick = {
                        viewModel.updateCameraDirection()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Gray.copy(
                            alpha = 0.5f
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "flip camera direction",
                        tint = Color.White // 아이콘 색상을 흰색으로 변경
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }

}

@Composable
fun CameraShutterButton(    onCapture: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .shadow(8.dp, shape = CircleShape)
            .background(color = Color.White, shape = CircleShape)
            .padding(4.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Black.copy(alpha = 0.7f))
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 리플 효과 제거
                onClick = onCapture
            )
    )
}

@Composable
fun CameraModeSelector(
    viewModel: MainViewModel,
    selectedMode: Int
) {
    val modes = listOf("AI Mode", "Normal Mode")

    ModeSelector(modes = modes, selectedMode = selectedMode, viewModel)
}

@Composable
fun ModeSelector(modes: List<String>, selectedMode: Int, viewModel: MainViewModel) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(modes.size) { index ->
            ModeItem(
                mode = modes[index],
                isSelected = selectedMode == index,
                onClick = { viewModel.updateCameraMode(index) }
            )
        }
    }
}

@Composable
fun ModeItem(mode: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier

            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) Color.DarkGray else Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = mode,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun SettingsBar(viewModel: MainViewModel) {
    val iso = viewModel.iso.collectAsState()
    val speed = viewModel.shutterSpeed.collectAsState()
    val ev = viewModel.ev.collectAsState()
    val focus = viewModel.focus.collectAsState()
    val wb = viewModel.wb.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f)),
        horizontalArrangement = Arrangement.SpaceEvenly

    ) {
        SettingItem(name = "ISO", value = iso.value)
        SettingItem(name = "Speed", value = speed.value)
        SettingItem(name = "EV", value = ev.value)
        SettingItem(name = "Focus", value = focus.value)
        SettingItem(name = "WB", value = wb.value)
    }
}

@Composable
fun <T> SettingItem(name: String, value: T) {

    Column(
        modifier = Modifier
            .padding(4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(name, color = Color.White, fontSize = 14.sp)
        Text(value.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}