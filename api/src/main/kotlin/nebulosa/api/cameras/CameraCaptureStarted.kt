package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.JobExecution
import java.time.Duration

data class CameraCaptureStarted(
    override val camera: Camera,
    val looping: Boolean,
    val estimatedTime: Duration,
    @JsonIgnore override val jobExecution: JobExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraCaptureEvent {

    val exposureAmount
        get() = tasklet.request.exposureAmount

    val exposureTime
        get() = tasklet.request.exposureTime

    override val progress = 0.0

    override val eventName = "CAMERA_CAPTURE_STARTED"
}
