package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceJobEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.JobExecution
import org.springframework.batch.item.ExecutionContext

data class CameraCaptureFinished(
    override val camera: Camera,
    override val jobExecution: JobExecution,
    override val executionContext: ExecutionContext = jobExecution.executionContext,
) : CameraCaptureEvent, SequenceJobEvent
