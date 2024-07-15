package com.example.smartcamera.utils

import kotlin.math.abs

class BodyRatioCalculator {
    private val ratioQueue = ArrayDeque<Float>()
    private val queueSize = 10 // 최근 10개 프레임 사용
    private var lastStableRatio = 0f
    private val updateThreshold = 0.05f // 5% 이상 차이날 때만 업데이트

    fun updateRatio(newRatio: Float): Float {
        ratioQueue.addLast(newRatio)
        if (ratioQueue.size > queueSize) {
            ratioQueue.removeFirst()
        }

        val averageRatio = ratioQueue.average().toFloat()

        if (abs(averageRatio - lastStableRatio) > updateThreshold) {
            lastStableRatio = averageRatio
        }

        return lastStableRatio
    }

    fun isIdealRatio(): Boolean {
        return lastStableRatio >= 1.54f
    }
}