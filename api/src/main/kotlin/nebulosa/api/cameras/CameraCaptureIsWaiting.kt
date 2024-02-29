package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.batch.processing.JobExecution
import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class CameraCaptureIsWaiting(
    @JsonIgnore override val jobExecution: JobExecution,
    override val camera: Camera,
    override val exposureAmount: Int,
    override val exposureCount: Int,
    override val captureElapsedTime: Duration,
    override val captureProgress: Double,
    override val captureRemainingTime: Duration,
    override val waitProgress: Double,
    override val waitRemainingTime: Duration,
    override val state: CameraCaptureState = CameraCaptureState.WAITING,
) : CameraCaptureElapsed {

    override val exposureProgress = 1.0
    override val exposureRemainingTime = Duration.ZERO!!
    override val savePath = null
}
