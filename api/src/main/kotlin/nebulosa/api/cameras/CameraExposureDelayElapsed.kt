package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution
import org.springframework.batch.item.ExecutionContext

data class CameraExposureDelayElapsed(
    override val camera: Camera,
    override val stepExecution: StepExecution,
    override val executionContext: ExecutionContext = stepExecution.executionContext,
) : CameraCaptureEvent, SequenceStepEvent
