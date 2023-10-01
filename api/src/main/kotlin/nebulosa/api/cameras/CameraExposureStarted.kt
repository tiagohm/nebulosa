package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution

data class CameraExposureStarted(
    override val camera: Camera,
    override val stepExecution: StepExecution,
) : CameraCaptureEvent, SequenceStepEvent {

    override val eventName = "CAMERA_EXPOSURE_STARTED"
}
