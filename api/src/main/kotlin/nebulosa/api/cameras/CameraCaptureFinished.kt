package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class CameraCaptureFinished(
    override val camera: Camera,
    override val exposureAmount: Int,
    override val captureElapsedTime: Duration,
) : CameraCaptureEvent {

    override val exposureCount = exposureAmount
    override val captureProgress = 1.0
    override val captureRemainingTime = Duration.ZERO!!
    override val exposureProgress = 1.0
    override val exposureRemainingTime = Duration.ZERO!!
    override val state = CameraCaptureState.CAPTURE_FINISHED
    override val waitRemainingTime = Duration.ZERO!!
    override val waitProgress = 0.0
    override val savePath = null
}
