package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class CameraCaptureStarted(
    override val camera: Camera,
    override val exposureAmount: Int,
    override val captureRemainingTime: Duration,
    override val exposureRemainingTime: Duration,
) : CameraCaptureEvent {

    override val exposureCount = 1
    override val captureElapsedTime = Duration.ZERO!!
    override val captureProgress = 0.0
    override val exposureProgress = 0.0
    override val state = CameraCaptureState.CAPTURE_STARTED
    override val waitRemainingTime = Duration.ZERO!!
    override val waitProgress = 0.0
    override val savePath = null
}
