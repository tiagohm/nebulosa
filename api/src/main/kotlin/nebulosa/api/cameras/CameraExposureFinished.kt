package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.imaging.Image
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution
import java.nio.file.Path

data class CameraExposureFinished(
    override val camera: Camera,
    override val stepExecution: StepExecution,
    val image: Image?,
    val savePath: Path?,
) : CameraCaptureEvent, SequenceStepEvent
