package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution
import java.nio.file.Path
import java.time.Duration

data class CameraExposureFinished(
    override val camera: Camera,
    override val exposureCount: Int,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
    val savePath: Path?,
) : CameraExposureEvent {

    override val remainingTime = Duration.ZERO!!

    override val progress = 1.0

    override val eventName = "CAMERA_EXPOSURE_FINISHED"
}
