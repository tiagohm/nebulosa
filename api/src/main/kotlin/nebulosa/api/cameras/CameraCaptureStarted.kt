package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.JobExecution
import kotlin.time.Duration

data class CameraCaptureStarted(
    override val camera: Camera,
    val isLoop: Boolean,
    val totalTime: Duration,
    @JsonIgnore override val jobExecution: JobExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraCaptureEvent {

    override val progress = 0.0

    override val eventName = "CAMERA_CAPTURE_STARTED"
}
