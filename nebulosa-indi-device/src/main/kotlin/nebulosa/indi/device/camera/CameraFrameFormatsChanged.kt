package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraFrameFormatsChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
