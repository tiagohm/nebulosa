package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraOffsetChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
