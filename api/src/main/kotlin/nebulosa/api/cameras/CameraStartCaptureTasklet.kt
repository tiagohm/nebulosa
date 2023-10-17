package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceTasklet

sealed interface CameraStartCaptureTasklet : SequenceTasklet<CameraCaptureEvent> {

    val request: CameraStartCaptureRequest
}
