package nebulosa.api.cameras

import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class CameraCaptureIsSettling(
    override val camera: Camera,
    override val exposureAmount: Int,
    override val exposureCount: Int,
    override val captureElapsedTime: Duration,
    override val captureProgress: Double,
    override val captureRemainingTime: Duration,
) : CameraCaptureEvent {

    override val state = CameraCaptureState.WAITING
    override val exposureProgress = 1.0
    override val exposureRemainingTime = Duration.ZERO!!
    override val savePath = null
    override val waitProgress = 0.0
    override val waitRemainingTime = Duration.ZERO!!
}
