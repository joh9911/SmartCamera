package com.example.smartcamera.objectdetector

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.PredefinedCategory
import java.nio.ByteBuffer

class MLObjectDetectorHelper(
    var objectDetectorListener: DetectorListener? = null,
) {

    private var objectDetector: ObjectDetector? = null

    init {
        initDetector()
    }

    private fun initDetector(){
        // Live detection and tracking
        val localModel = LocalModel.Builder()
            .setAssetFilePath("ef.tflite")
            .build()
        val options = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .setMaxPerObjectLabelCount(3)
            .build()
       objectDetector = ObjectDetection.getClient(options)
    }

    @OptIn(ExperimentalGetImage::class)
    fun detectAsync(image: ImageProxy){
        val realImage = image.image
        if (realImage != null){
            val i = InputImage.fromMediaImage(realImage, image.imageInfo.rotationDegrees)
            objectDetector?.process(i)?.addOnSuccessListener { detectedObjects ->
                objectDetectorListener?.onResults(detectedObjects, image.width, image.height)

            }
                ?.addOnFailureListener {
                    Log.d("mlObjectDetectorHelper","${it}")

                }
                ?.addOnCompleteListener {
                    image.close()
                }
        }


    }

    // Extension function to convert ImageProxy to Bitmap
    fun ImageProxy.toBitmap(): Bitmap {
        val plane = planes[0]
        val buffer: ByteBuffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }

    // Extension function to rotate Bitmap
    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    interface DetectorListener {
        fun onError(error: String, errorCode: Int = ObjectDetectorHelper.OTHER_ERROR)
        fun onResults(detectedObjects: MutableList<DetectedObject>, width: Int, height: Int)
    }
}