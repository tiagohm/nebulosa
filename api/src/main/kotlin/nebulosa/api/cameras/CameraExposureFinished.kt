package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.batch.processing.JobExecution
import nebulosa.indi.device.camera.Camera
import java.nio.file.Path
import java.time.Duration

data class CameraExposureFinished(
    @JsonIgnore override val jobExecution: JobExecution,
    override val camera: Camera,
    override val exposureAmount: Int,
    override val exposureCount: Int,
    override val captureElapsedTime: Duration,
    override val captureProgress: Double,
    override val captureRemainingTime: Duration,
    override val savePath: Path,
) : CameraExposureEvent {

    override val state = CameraCaptureState.EXPOSURE_FINISHED
    override val exposureProgress = 0.0
    override val exposureRemainingTime = Duration.ZERO!!
    override val waitProgress = 0.0
    override val waitRemainingTime = Duration.ZERO!!
}

