package com.example.smartcamera.home.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.smartcamera.objectdetector.ObjectDetectorHelper

class ImageAnalyzer(
    private val objectDetectorHelper: ObjectDetectorHelper,
//    private val faceDetectorHelper: FaceDetectorHelper,
): ImageAnalysis.Analyzer {

    private var frameCounter = 0
    override fun analyze(image: ImageProxy) {
        try {
            if (frameCounter % 30 == 0) {
                objectDetectorHelper.detectLivestreamFrame(image)
//                faceDetectorHelper.detectLivestreamFrame(image)
            }
        } catch (e: Exception) {
            Log.e("ImageAnalyzer", "Error processing image", e)
        } finally {
            frameCounter++
            image.close()
        }


    }
}