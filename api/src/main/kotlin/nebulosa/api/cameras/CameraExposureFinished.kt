package nebulosa.api.cameras

import com.fasterxml.jackson.annotation.JsonIgnore
import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution
import java.nio.file.Path

data class CameraExposureFinished(
    override val camera: Camera,
    @JsonIgnore override val stepExecution: StepExecution,
    @JsonIgnore override val tasklet: CameraExposureTasklet,
    val image: Image?, val savePath: Path?,
) : CameraCaptureEvent, SequenceStepEvent {

    override val eventName = "CAMERA_EXPOSURE_FINISHED"
}
