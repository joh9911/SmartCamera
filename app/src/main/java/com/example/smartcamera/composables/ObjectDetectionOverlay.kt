
package com.example.smartcamera.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectionResult



@Composable
fun ObjectDetectionOverlay(
    objectDetectionResults: ObjectDetectionResult,
    frameWidth: Int,
    frameHeight: Int,
) {
    val objectDetections = objectDetectionResults.detections()
    if (objectDetections != null) {
        for (detection in objectDetections) {
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
            ) {
                if (detection.categories() == null) return@BoxWithConstraints
                if (detection.categories().first() == null) return@BoxWithConstraints
                if (detection.categories().first().categoryName() != "person") return@BoxWithConstraints
                // calculating the UI dimensions of the detection bounds
                val resultBounds = detection.boundingBox()
                val boxWidth = (resultBounds.width() / frameWidth) * this.maxWidth.value
                val boxHeight = (resultBounds.height() / frameHeight) * this.maxHeight.value
                val boxLeftOffset = (resultBounds.left / frameWidth) * this.maxWidth.value
                val boxTopOffset = (resultBounds.top / frameHeight) * this.maxHeight.value

                Box(
                    Modifier
                        .fillMaxSize()
                        .offset(
                            boxLeftOffset.dp,
                            boxTopOffset.dp,
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .border(3.dp, Color.Gray)
                            .width(boxWidth.dp)
                            .height(boxHeight.dp)
                    )
                    Box(modifier = Modifier.padding(3.dp)) {
                        Text(
                            text = "${
                                detection.categories().first().categoryName()
                            } ${detection.categories().first().score().toString().take(4)}",
                            modifier = Modifier
                                .background(Color.Black)
                                .padding(5.dp, 0.dp),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}