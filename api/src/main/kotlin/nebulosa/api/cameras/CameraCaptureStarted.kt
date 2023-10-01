package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceJobEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.JobExecution

data class CameraCaptureStarted(
    override val camera: Camera,
    override val jobExecution: JobExecution,
) : CameraCaptureEvent, SequenceJobEvent {

    override val eventName = "CAMERA_CAPTURE_STARTED"
}
