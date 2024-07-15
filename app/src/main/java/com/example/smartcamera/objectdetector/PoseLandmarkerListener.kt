package com.example.smartcamera.objectdetector

class PoseLandmarkerListener(
    val onErrorCallback: (error: String, errorCode: Int) -> Unit,
    val onResultsCallback: (resultBundle: PoseLandmarkerHelper.ResultBundle) -> Unit,
): PoseLandmarkerHelper.LandmarkerListener {
    override fun onError(error: String, errorCode: Int) {
        onErrorCallback(error, errorCode)
    }

    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        onResultsCallback(resultBundle)
    }

}