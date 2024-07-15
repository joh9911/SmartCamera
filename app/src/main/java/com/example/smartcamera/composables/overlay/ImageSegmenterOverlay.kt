package com.example.smartcamera.composables

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.example.smartcamera.objectdetector.ImageSegmenterHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

@Composable
fun ImageSegmenterOverlay(
    byteBuffer: ByteBuffer?,
    outputWidth: Int,
    outputHeight: Int,
    runningMode: RunningMode = RunningMode.IMAGE,
    modifier: Modifier = Modifier
) {
    var scaleFactor by remember { mutableStateOf(1f) }
    var scaleBitmap by remember { mutableStateOf<Bitmap?>(null) }

    if (byteBuffer != null) {
        // Create the mask bitmap with colors and the set of detected labels.
        val pixels = IntArray(byteBuffer.capacity())
        for (i in pixels.indices) {
            val index = byteBuffer.get(i).toUInt()
            val color = if (index == 0U) {
                Color(0x7FADD8E6).toArgb() // 인물을 옅은 파란색으로 설정
            } else {
                Color.Transparent.toArgb() // 배경을 투명색으로 설정
            }
            pixels[i] = color
        }

        // 테두리를 검출하여 흰색으로 설정
//        val edgePixels = detectEdges(pixels, outputWidth, outputHeight)
//        edgePixels.forEach { (x, y) ->
//            pixels[y * outputWidth + x] = Color.White.toArgb()
//        }

        val image = Bitmap.createBitmap(
            pixels,
            outputWidth,
            outputHeight,
            Bitmap.Config.ARGB_8888
        )

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                min(
                    LocalContext.current.resources.displayMetrics.widthPixels * 1f / outputWidth,
                    LocalContext.current.resources.displayMetrics.heightPixels * 1f / outputHeight
                )
            }
            RunningMode.LIVE_STREAM -> {
                max(
                    LocalContext.current.resources.displayMetrics.widthPixels * 1f / outputWidth,
                    LocalContext.current.resources.displayMetrics.heightPixels * 1f / outputHeight
                )
            }
        }

        val scaleWidth = (outputWidth * scaleFactor).toInt()
        val scaleHeight = (outputHeight * scaleFactor).toInt()

        scaleBitmap = Bitmap.createScaledBitmap(image, scaleWidth, scaleHeight, false)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        scaleBitmap?.let {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawBitmap(it, 0f, 0f, null)
            }
        }
    }
}

fun detectEdges(pixels: IntArray, width: Int, height: Int): List<Pair<Int, Int>> {
    val edges = mutableListOf<Pair<Int, Int>>()
    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            val currentIndex = y * width + x
            val currentColor = pixels[currentIndex]
            if (currentColor != Color.Transparent.toArgb()) {
                val neighbors = listOf(
                    pixels[(y - 1) * width + x], // 상
                    pixels[(y + 1) * width + x], // 하
                    pixels[y * width + (x - 1)], // 좌
                    pixels[y * width + (x + 1)]  // 우
                )
                if (neighbors.any { it == Color.Transparent.toArgb() }) {
                    edges.add(x to y)
                }
            }
        }
    }
    return edges
}
