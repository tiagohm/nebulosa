package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.batch.processing.JobExecution
import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class CameraCaptureStarted(
    @JsonIgnore override val jobExecution: JobExecution,
    override val camera: Camera,
    override val exposureAmount: Int,
    override val captureRemainingTime: Duration,
    override val exposureRemainingTime: Duration,
) : CameraCaptureElapsed {

    override val exposureCount = 1
    override val captureElapsedTime = Duration.ZERO!!
    override val captureProgress = 0.0
    override val exposureProgress = 0.0
    override val state = CameraCaptureState.CAPTURE_STARTED
    override val waitRemainingTime = Duration.ZERO!!
    override val waitProgress = 0.0
    override val savePath = null
}
