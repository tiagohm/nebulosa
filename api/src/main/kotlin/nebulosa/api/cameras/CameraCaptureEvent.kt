package nebulosa.api.cameras

import nebulosa.api.messages.MessageEvent
import nebulosa.indi.device.camera.Camera

sealed interface CameraCaptureEvent : MessageEvent {

    val camera: Camera

    val progress: Double
}
