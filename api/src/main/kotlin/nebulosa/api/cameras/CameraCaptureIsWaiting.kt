package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class CameraCaptureIsWaiting(
    override val camera: Camera,
    override val exposureAmount: Int,
    override val exposureCount: Int,
    override val captureElapsedTime: Duration,
    override val captureProgress: Double,
    override val captureRemainingTime: Duration,
    override val waitProgress: Double,
    override val waitRemainingTime: Duration,
) : CameraCaptureEvent {

    override val state = CameraCaptureState.WAITING
    override val exposureProgress = 1.0
    override val exposureRemainingTime = Duration.ZERO!!
    override val savePath = null
}
