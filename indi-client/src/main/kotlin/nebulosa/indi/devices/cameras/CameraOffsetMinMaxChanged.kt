package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraOffsetMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
