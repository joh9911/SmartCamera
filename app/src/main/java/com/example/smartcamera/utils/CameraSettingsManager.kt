import android.annotation.SuppressLint
import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import com.example.smartcamera.ui.viewmodel.MainViewModel

@SuppressLint("RestrictedApi")
@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
class CameraSettingsManager(private val viewModel: MainViewModel) {

    private val camera2CameraControl: Camera2CameraControl?
        get() = viewModel.camera2CameraControl.value

    // 셔터 스피드 설정
    fun setShutterSpeed(shutterSpeed: Long) {
        val captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, shutterSpeed)
            .build()
        camera2CameraControl?.setCaptureRequestOptions(captureRequestOptions)
    }

    // ISO 설정
    fun setISO(iso: Int) {

        val captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, iso)
            .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, 1_000_000_000L / 60)
            .build()
        camera2CameraControl?.setCaptureRequestOptions(captureRequestOptions)
    }

    // 화이트 밸런스 설정
    fun setWhiteBalanceMode(whiteBalanceMode: Int) {
        val captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, whiteBalanceMode)
            .build()
        camera2CameraControl?.setCaptureRequestOptions(captureRequestOptions)
    }

    // 초점 거리 설정
    fun setFocusDistance(focusDistance: Float) {
        val captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance)
            .build()
        camera2CameraControl?.setCaptureRequestOptions(captureRequestOptions)
    }

    // 노출 보정 설정
    fun setExposureCompensation(exposureCompensation: Int) {
        val captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCompensation)
            .build()
        camera2CameraControl?.setCaptureRequestOptions(captureRequestOptions)
    }

    // 플래시 모드 설정
    fun setFlashMode(flashMode: Int) {
        val captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.FLASH_MODE, flashMode)
            .build()
        camera2CameraControl?.setCaptureRequestOptions(captureRequestOptions)
    }

    // 자동 노출 모드 설정
    fun setAEMode(aeMode: Int) {
        val captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, aeMode)
            .build()
        camera2CameraControl?.setCaptureRequestOptions(captureRequestOptions)
    }

    // 자동 화이트 밸런스 모드 설정
    fun setAWBMode(awbMode: Int) {
        val captureRequestOptions = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, awbMode)
            .build()
        camera2CameraControl?.setCaptureRequestOptions(captureRequestOptions)
    }
}
