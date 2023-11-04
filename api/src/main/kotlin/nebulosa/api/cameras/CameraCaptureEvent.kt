package nebulosa.api.cameras

import nebulosa.api.sequencer.SequenceJobEvent
import nebulosa.api.sequencer.SequenceTaskletEvent
import nebulosa.api.services.MessageEvent
import nebulosa.indi.device.camera.Camera

sealed interface CameraCaptureEvent : MessageEvent, SequenceTaskletEvent, SequenceJobEvent {

    val camera: Camera
}
