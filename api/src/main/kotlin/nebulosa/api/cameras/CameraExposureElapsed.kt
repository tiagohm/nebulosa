package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.batch.processing.JobExecution
import nebulosa.indi.device.camera.Camera
import java.time.Duration

data class CameraExposureElapsed(
    @JsonIgnore override val jobExecution: JobExecution,
    override val camera: Camera,
    override val exposureAmount: Int,
    override val exposureCount: Int,
    override val captureElapsedTime: Duration,
    override val captureProgress: Double,
    override val captureRemainingTime: Duration,
    override val exposureProgress: Double,
    override val exposureRemainingTime: Duration,
) : CameraCaptureElapsed {

    override val state = CameraCaptureState.EXPOSURING
    override val waitProgress = 0.0
    override val waitRemainingTime = Duration.ZERO!!
    override val savePath = null
}
