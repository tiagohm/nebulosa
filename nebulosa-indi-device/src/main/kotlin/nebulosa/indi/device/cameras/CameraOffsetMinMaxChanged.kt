package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraOffsetMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
