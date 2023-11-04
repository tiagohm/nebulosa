package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution
import java.time.Duration

data class CameraCaptureIsWaiting(
    override val camera: Camera,
    val waitDuration: Duration,
    val remainingTime: Duration,
    override val progress: Double,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraCaptureEvent, SequenceStepEvent {

    override val eventName = "CAMERA_CAPTURE_WAITING"
}
