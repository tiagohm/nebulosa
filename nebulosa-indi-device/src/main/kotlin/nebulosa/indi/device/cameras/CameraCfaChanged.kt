package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCfaChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
