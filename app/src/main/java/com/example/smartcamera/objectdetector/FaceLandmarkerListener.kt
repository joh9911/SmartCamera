package com.example.smartcamera.objectdetector

class FaceLandmarkerListener(
    val onErrorCallback: (error: String, errorCode: Int) -> Unit,
    val onResultsCallback: (resultBundle: FaceLandmarkerHelper.ResultBundle) -> Unit,
    val onEmptyCallback: () -> Unit
): FaceLandmarkerHelper.LandmarkerListener {
    override fun onError(error: String, errorCode: Int) {
        onErrorCallback(error, errorCode)
    }

    override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
        onResultsCallback(resultBundle)
    }

    override fun onEmpty() {
        onEmptyCallback()
    }
}