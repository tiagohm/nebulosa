package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceStepEvent
import nebulosa.indi.device.camera.Camera
import org.springframework.batch.core.StepExecution

data class CameraExposureUpdated(
    override val camera: Camera,
    override val stepExecution: StepExecution,
) : CameraCaptureEvent, SequenceStepEvent
