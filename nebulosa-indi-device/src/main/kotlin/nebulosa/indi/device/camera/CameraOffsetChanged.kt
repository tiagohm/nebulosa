package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraOffsetChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
