package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraCapturingChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
