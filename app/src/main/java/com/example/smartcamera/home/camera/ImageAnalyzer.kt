package com.example.smartcamera.home.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.collectAsState
import com.example.smartcamera.objectdetector.FaceLandmarkerHelper
import com.example.smartcamera.objectdetector.ImageSegmenterHelper
import com.example.smartcamera.objectdetector.MLObjectDetectorHelper
import com.example.smartcamera.objectdetector.PoseLandmarkerHelper
import com.example.smartcamera.ui.viewmodel.MainViewModel
import com.example.smartcamera.utils.GuideMessage
import com.example.smartcamera.utils.Tag

class ImageAnalyzer(
    private val viewModel: MainViewModel,
    private val lensFacing: Int,
    private val faceLandmarkerHelper: FaceLandmarkerHelper,
    private val mlObjectDetectorHelper: MLObjectDetectorHelper,
    private val imageSegmenterHelper: ImageSegmenterHelper,
    private val poseLandmarkerHelper: PoseLandmarkerHelper
    ) : ImageAnalysis.Analyzer {




    private var frameCounter = 0

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
        val rotationDegrees = image.imageInfo.rotationDegrees
        if (rotationDegrees == 0 || rotationDegrees == 180) {
            viewModel.setImageSourceInfo(image.width, image.height, isImageFlipped)
        } else {
            viewModel.setImageSourceInfo(image.height, image.width, isImageFlipped)
        }
        try {
//            mlObjectDetectorHelper.detectAsync(image)
            poseLandmarkerHelper.detectLiveStream(image,true)

            if (frameCounter % 15 == 0) { // 예: 30 프레임마다 확인
                if (viewModel.isTrackingIdStale()) {
                    viewModel.setGuideMessageActive(false)
                }
            }

            when (viewModel.guideState){
                MainViewModel.GuideState.Idle -> {}
                MainViewModel.GuideState.ObjectSelected -> {
                    viewModel.setGuideMessage(GuideMessage.LOCATE_TO_CERTAIN_AREA)
                }
                MainViewModel.GuideState.PositionGuide -> {
                    viewModel.setGuideMessage(GuideMessage.LOWER_CAMERA)
                }
                MainViewModel.GuideState.GuideComplete -> {}
            }
//            imageSegmenterHelper.segmentLiveStreamFrame(image, true)
//            faceLandmarkerHelper.detectLiveStream(image, true)

        } catch (e: Exception) {
            Log.e(Tag.TAG, "imageAnalyzer error", e)
        } finally {
            frameCounter++
        }


    }
}