package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCapturingChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
