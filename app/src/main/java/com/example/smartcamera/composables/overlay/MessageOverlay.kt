package com.example.smartcamera.composables.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.smartcamera.utils.GuideMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageOverlay(message: GuideMessage, isVisible: Boolean, onDismiss: () -> Unit) {
    var displayMessage by remember { mutableStateOf(GuideMessage.EMPTY) }
    val coroutineScope = rememberCoroutineScope()
    val messageProgress = remember { Animatable(0f) }
    val boxWidth = messageProgress.value * 300.dp // Assume max width of 300dp for the message box

    LaunchedEffect(message) {

        displayMessage = message

    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable {
                    onDismiss()
                }
        ) {
            Text(
                text = displayMessage.message,
                color = Color.White,
                fontSize = 14.sp
            )
        }

    }
}