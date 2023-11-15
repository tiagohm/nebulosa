package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.JobExecution

data class CameraCaptureFinished(
    override val camera: Camera,
    @JsonIgnore override val jobExecution: JobExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraCaptureEvent {

    override val progress = 1.0

    override val eventName = "CAMERA_CAPTURE_FINISHED"
}
