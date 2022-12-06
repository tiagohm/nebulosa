package nebulosa.api.cameras

import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.events.CameraEvent

data class CameraCaptureFinishedEvent(override val device: Camera) : CameraEvent
