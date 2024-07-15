package com.example.smartcamera.objectdetector

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.smartcamera.ui.viewmodel.ObjectFocusedOnState

abstract class Graphic(private val overlayState: ObjectFocusedOnState) {

    abstract fun draw(drawScope: DrawScope, state: ObjectFocusedOnState)

    fun drawRect(
        drawScope: DrawScope,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        paint: Paint,
    ) {
        drawScope.drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = paint.strokeWidth)
        )
    }

    fun drawText(drawScope: DrawScope, text: String, x: Float, y: Float, paint: Paint) {
        drawScope.drawContext.canvas.nativeCanvas.drawText(
            text,
            x,
            y,
            paint
        )
    }

    fun scale(imagePixel: Float): Float {
        return overlayState.scale(imagePixel)
    }

    fun translateX(x: Float): Float {
        return overlayState.translateX(x)
    }

    fun translateY(y: Float): Float {
        return overlayState.translateY(y)
    }
}
