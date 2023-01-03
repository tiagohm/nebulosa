package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraIsCapturingChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
