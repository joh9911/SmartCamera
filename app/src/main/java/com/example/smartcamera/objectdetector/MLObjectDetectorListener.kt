package com.example.smartcamera.objectdetector

import com.google.mlkit.vision.objects.DetectedObject

class MLObjectDetectorListener(
    val onErrorCallback: (error: String, errorCode: Int) -> Unit,
    val onResultsCallback: (detectedObjects: MutableList<DetectedObject>, width: Int, height: Int) -> Unit
): MLObjectDetectorHelper.DetectorListener {
    override fun onError(error: String, errorCode: Int) {

    }

    override fun onResults(detectedObjects: MutableList<DetectedObject>, width: Int, height: Int) {
        onResultsCallback(detectedObjects, width, height)
    }
}