package com.example.smartcamera.composables.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.smartcamera.ui.viewmodel.MainViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Composable
fun ObjectFocusedOnOverlay(viewModel: MainViewModel) {
    val cameraMode by viewModel.cameraMode.collectAsState()
    val state by viewModel.graphicOverlayState.collectAsState()


//    val pitch by viewModel.pitch.collectAsState()
//    val roll by viewModel.roll.collectAsState()
//
//    // 이전 값들을 저장할 상태를 추가 합니다.
//    var previousPitch by remember { mutableStateOf(0f) }
//    var previousRoll by remember { mutableStateOf(0f) }
//
//    // 필터링 된 값들을 계산 합니다.
//    val filteredPitch = (pitch + previousPitch) / 2
//    val filteredRoll = (roll + previousRoll) / 2
//
//    // 이전 값을 업데이트 합니다.
//    previousPitch = filteredPitch
//    previousRoll = filteredRoll

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                if (cameraMode == 0) {
                    coroutineScope {
                        launch {
                            detectTapGestures { offset ->
                                viewModel.updateClickedPoint(offset)
                            }
                        }
                    }
                }

            }) {
        viewModel.updateTransformationIfNeeded(size.width.toInt(), size.height.toInt())

        // 초기 그리드 색상
        val initialGridColor = Color.White.copy(alpha = 0.5f)
        val highlightColor = Color.Yellow.copy(alpha = 0.8f)
        val numColumns = 3
        val numRows = 3
        val cellWidth = size.width / numColumns
        val cellHeight = size.height / numRows

        // 초기 상태로 그리드 그리기
//        for (i in 1 until numColumns) {
//            drawLine(
//                start = Offset(x = cellWidth * i, y = 0f),
//                end = Offset(x = cellWidth * i, y = size.height),
//                color = initialGridColor,
//                strokeWidth = 1.dp.toPx()
//            )
//        }
//        for (i in 1 until numRows) {
//            drawLine(
//                start = Offset(x = 0f, y = cellHeight * i),
//                end = Offset(x = size.width, y = cellHeight * i),
//                color = initialGridColor,
//                strokeWidth = 1.dp.toPx()
//            )
//        }

        // 중앙과 하단의 특정 셀 경계 위치
        val centerCellTopLeft = Offset(cellWidth, cellHeight)
        val centerCellSize = Size(cellWidth, cellHeight)
        val bottomCellTopLeft = Offset(cellWidth, 2 * cellHeight)
        val bottomCellSize = Size(cellWidth, cellHeight)

        // 물체 박스가 중앙 또는 하단 셀에 들어가면 해당 셀의 테두리를 노란색으로 변경

        for (graphic in state.graphics) {
            graphic.draw(this, state)
            if (state.trackedTrackingIds.contains(graphic.detectedObject.trackingId)){

                val rect = graphic.detectedObject.boundingBox
                val x0 = state.translateX(rect.left.toFloat())
                val x1 = state.translateX(rect.right.toFloat())
                val rectLeft = min(x0, x1)
                val rectRight = max(x0, x1)
                val y0 = state.translateY(rect.top.toFloat())
                val y1 = state.translateY(rect.bottom.toFloat())
                val rectTop = min(y0, y1)
                val rectBottom = max(y0, y1)

                val isInCenterCell = rectLeft < centerCellTopLeft.x + centerCellSize.width &&
                        rectRight > centerCellTopLeft.x &&
                        rectTop < centerCellTopLeft.y + centerCellSize.height &&
                        rectBottom > centerCellTopLeft.y

                val isInBottomCell = rectLeft < bottomCellTopLeft.x + bottomCellSize.width &&
                        rectRight > bottomCellTopLeft.x &&
                        rectTop < bottomCellTopLeft.y + bottomCellSize.height &&
                        rectBottom > bottomCellTopLeft.y



//                if (isInCenterCell) {
//                    drawRect(
//                        color = highlightColor,
//                        topLeft = centerCellTopLeft,
//                        size = centerCellSize,
//                        style = Stroke(width = 1.dp.toPx())
//                    )
//                }
//                if (isInBottomCell) {
//                    drawRect(
//                        color = highlightColor,
//                        topLeft = bottomCellTopLeft,
//                        size = bottomCellSize,
//                        style = Stroke(width = 1.dp.toPx())
//                    )
//                }

            }

        }


//        if (state.isGuideModeOn){
//            val highlightColor = Color.Yellow.copy(alpha = 0.5f)
//            val numColumns = 3
//            val cellWidth = size.width / numColumns
//            val cellHeight = size.height / numColumns
//
//            // 강조할 부분 (중앙과 하단 셀) 그리기
//            val centerCellTop = cellHeight
//            val centerCellBottom = centerCellTop + cellHeight
//            drawRect(
//                color = highlightColor,
//                topLeft = Offset(cellWidth, centerCellTop),
//                size = Size(cellWidth, cellHeight),
//                style = Stroke(width = 3.dp.toPx())
//            )
//
//            val bottomCellTop = cellHeight * 2
//            drawRect(
//                color = highlightColor,
//                topLeft = Offset(cellWidth, bottomCellTop),
//                size = Size(cellWidth, cellHeight),
//                style = Stroke(width = 3.dp.toPx())
//            )
//        }

//        val centerX = size.width / 2
//        val centerY = size.height / 2
//        val lineLength = size.width / 2  // 화면 너비의 절반을 선의 길이로 설정
//
//        // 수평 상태를 판단하기 위한 임계값 설정
//        val pitchThreshold = 10f  // pitch 임계값 (기존보다 확대)
//        val rollThreshold = 10f   // roll 임계값 (기존보다 확대)
//
//        // 수평 상태 여부 확인
//        val isHorizontal = (abs(filteredPitch) < pitchThreshold) && (abs(filteredRoll) < rollThreshold)
//
//        // 선의 색상 결정
//        val lineColor = if (isHorizontal) Color.White.copy(alpha = 0.7f) else Color.Yellow
//
//        // 회전 각도를 좀 더 세밀하게 조정
//        val rotationAngle = filteredRoll / 3  // 회전 각도를 줄여서 적용
//
//        // 중심점을 기준으로 회전하여 선을 그립니다.
//        rotate(degrees = rotationAngle, pivot = androidx.compose.ui.geometry.Offset(centerX, centerY)) {
//            drawLine(
//                color = lineColor,
//                start = androidx.compose.ui.geometry.Offset(centerX - lineLength / 2, centerY),
//                end = androidx.compose.ui.geometry.Offset(centerX + lineLength / 2, centerY),
//                strokeWidth = 5f
//            )
//        }
    }
}