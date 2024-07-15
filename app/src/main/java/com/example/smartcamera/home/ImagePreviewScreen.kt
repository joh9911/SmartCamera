package com.example.smartcamera.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartcamera.ui.viewmodel.MainViewModel



@Composable
fun ImagePreviewScreen(
    viewModel: MainViewModel,
    onBackPressed: () -> Unit
) {
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        capturedImageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } ?: run {
            // 이미지가 없는 경우 표시할 내용
            Text(
                "No image captured",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 뒤로 가기 버튼
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}