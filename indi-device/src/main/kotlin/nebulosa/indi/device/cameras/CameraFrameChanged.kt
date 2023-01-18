package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraFrameChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
