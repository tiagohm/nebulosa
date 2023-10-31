package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution

data class CameraExposureStarted(
    override val camera: Camera,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraCaptureEvent, SequenceStepEvent {

    override val eventName = "CAMERA_EXPOSURE_STARTED"
}
