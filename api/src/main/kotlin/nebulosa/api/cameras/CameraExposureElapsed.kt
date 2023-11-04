package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution
import java.time.Duration

data class CameraExposureElapsed(
    override val camera: Camera,
    override val exposureCount: Int,
    override val remainingTime: Duration,
    override val progress: Double,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraExposureEvent {

    override val eventName = "CAMERA_EXPOSURE_ELAPSED"
}
