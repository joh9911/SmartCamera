package com.example.smartcamera.objectdetector

class FaceDetectorListener(
    val onErrorCallback: (error: String, errorCode: Int) -> Unit,
    val onResultsCallback: (resultBundle: FaceDetectorHelper.ResultBundle) -> Unit
) : FaceDetectorHelper.DetectorListener {

    override fun onError(error: String, errorCode: Int) {
        onErrorCallback(error, errorCode)
    }

    override fun onResults(resultBundle: FaceDetectorHelper.ResultBundle) {
        onResultsCallback(resultBundle)
    }


}