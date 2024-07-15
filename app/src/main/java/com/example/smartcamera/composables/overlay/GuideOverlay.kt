package com.example.smartcamera.composables.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectionResult

@Composable
fun GuideOverlay(
    previewWidth: Float,
    previewHeight: Float,
    footOffsetFromBottom: Float = 70f, // 발 위치를 바닥에서 50px 위로 설정
    headOffsetFromGridCenter: Float = 50f, // 머리 위치를 위에서 200px 아래로 설정
    footSize: Float = 300f, // 발의 크기를 나타내는 평균 값
    headSize: Float = 25f, // 머리의 크기를 나타내는 평균 값


    ) {
    Canvas(
        modifier = Modifier
            .width(previewWidth.dp)
            .height(previewHeight.dp)
    ) {
        // 발 위치 선
//        val headY = size.height / 2 - headOffsetFromGridCenter
//        val headXStart = size.width / 2 - footSize / 2
//        val headXEnd = size.width / 2 + footSize / 2
//        drawLine(
//            color = Color.Blue,
//            start = Offset(x = headXStart, y = headY),
//            end = Offset(x = headXEnd, y = headY),
//            strokeWidth = 4.dp.toPx()
//        )

        // 발 위치 선
        val footY = size.height - footOffsetFromBottom
        val footXStart = size.width / 2 - footSize / 2
        val footXEnd = size.width / 2 + footSize / 2
        drawLine(
            color = Color.White,
            start = Offset(x = footXStart, y = footY),
            end = Offset(x = footXEnd, y = footY),
            strokeWidth = 2.dp.toPx()
        )
    }
}