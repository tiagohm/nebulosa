package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraOffsetMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
