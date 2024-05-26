
package com.example.smartcamera.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.smartcamera.R


@Composable
fun MediaPipeBanner(
    onOptionsButtonClick: (() -> Unit)? = null,
    onBackButtonClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xEEEEEEEE)),
    ) {
        if (onBackButtonClick != null) {
            IconButton(
                onClick = onBackButtonClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Backward arrow icon",
                    tint = Color.Gray
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.media_pipe_banner),
            contentDescription = "MediaPipe logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.align(Alignment.Center)
        )
        if (onOptionsButtonClick != null) {
            IconButton(
                onClick = onOptionsButtonClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings icon",
                    tint = Color.Gray
                )
            }
        }
    }
}