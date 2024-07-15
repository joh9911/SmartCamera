package com.example.smartcamera.utils

enum class GuideMessage(val message: String) {
    EMPTY(""),
    FACE_NOT_DETECTED("얼굴이 인식되지 않았습니다. 카메라를 얼굴 쪽으로 향해주세요."),
    MOVE_CAMERA_DOWN("카메라를 약간 아래로 향해주세요."),
    MOVE_CAMERA_UP("카메라를 약간 위로 향해주세요."),
    MOVE_CLOSER("카메라를 얼굴에 더 가까이 가져가주세요."),
    MOVE_FARTHER("카메라를 얼굴에서 조금 멀리 떨어뜨려주세요."),
    STRAIGHTEN_HEAD("머리를 수평으로 맞춰주세요."),
    TILT_CAMERA_DOWN("카메라를 약간 아래로 기울여주세요."),
    TILT_CAMERA_UP("카메라를 약간 위로 기울여주세요."),
    GOOD_POSE("좋은 자세입니다! 사진을 찍어보세요.")
}