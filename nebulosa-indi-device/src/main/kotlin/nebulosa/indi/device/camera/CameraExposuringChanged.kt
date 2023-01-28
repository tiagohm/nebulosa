package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraExposuringChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
