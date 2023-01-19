package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposuringChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
