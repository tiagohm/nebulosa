package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraFrameChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
