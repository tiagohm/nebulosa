package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution

data class CameraExposureStarted(
    override val camera: Camera,
    override val exposureCount: Int,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraExposureEvent {

    override val remainingTime
        get() = tasklet.request.exposureTime

    override val progress = 0.0

    override val eventName = "CAMERA_EXPOSURE_STARTED"
}
