package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraOffsetChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
