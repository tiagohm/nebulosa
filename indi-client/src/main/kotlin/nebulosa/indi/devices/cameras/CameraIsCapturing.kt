package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraIsCapturing(override val device: Camera) : CameraEvent, PropertyChangedEvent
