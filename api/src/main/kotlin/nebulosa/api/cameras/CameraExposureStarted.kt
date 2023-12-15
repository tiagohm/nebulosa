package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class CameraExposureStarted(
    override val camera: Camera,
    override val exposureAmount: Int,
    override val exposureCount: Int,
    override val captureElapsedTime: Duration,
    override val captureProgress: Double,
    override val captureRemainingTime: Duration,
    override val exposureRemainingTime: Duration,
) : CameraExposureEvent {

    override val state = CameraCaptureState.EXPOSURE_STARTED
    override val exposureProgress = 0.0
    override val waitProgress = 0.0
    override val waitRemainingTime = Duration.ZERO!!
    override val savePath = null
}
