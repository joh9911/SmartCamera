/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.smartcamera.objectdetector

import com.example.smartcamera.objectdetector.ObjectDetectorHelper



class ObjectDetectorListener(
    val onErrorCallback: (error: String, errorCode: Int) -> Unit,
    val onResultsCallback: (resultBundle: ObjectDetectorHelper.ResultBundle) -> Unit
) : ObjectDetectorHelper.DetectorListener {

    override fun onError(error: String, errorCode: Int) {
        onErrorCallback(error, errorCode)
    }

    override fun onResults(resultBundle: ObjectDetectorHelper.ResultBundle) {
        onResultsCallback(resultBundle)
    }
}