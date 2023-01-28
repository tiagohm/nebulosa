package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraFrameChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
