package com.example.smartcamera.objectdetector

class ImageSegmenterListener(
    val onErrorCallback: (error: String, errorCode: Int) -> Unit,
    val onResultsCallback: (resultBundle: ImageSegmenterHelper.ResultBundle) -> Unit,
): ImageSegmenterHelper.SegmenterListener {
    override fun onError(error: String, errorCode: Int) {
        onErrorCallback(error, errorCode)
    }

    override fun onResults(resultBundle: ImageSegmenterHelper.ResultBundle) {
        onResultsCallback(resultBundle)
    }
}