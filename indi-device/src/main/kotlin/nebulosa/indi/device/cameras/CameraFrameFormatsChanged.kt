package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraFrameFormatsChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
