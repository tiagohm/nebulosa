package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution
import java.time.Duration

data class CameraCaptureElapsed(
    override val camera: Camera,
    val exposureCount: Int,
    val remainingTime: Duration,
    override val progress: Double,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
) : CameraCaptureEvent, SequenceStepEvent {

    override val eventName = "CAMERA_CAPTURE_ELAPSED"
}
