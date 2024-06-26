package com.example.smartcamera.utils

import androidx.camera.core.AspectRatio

enum class CameraAspectRatio(val ratio: Int) {
    RATIO_4_3(AspectRatio.RATIO_4_3),
    RATIO_16_9(AspectRatio.RATIO_16_9),
    RATIO_1_1(-1),  // Custom ratio
    FULL_SCREEN(-2) // Custom full-screen ratio
}
