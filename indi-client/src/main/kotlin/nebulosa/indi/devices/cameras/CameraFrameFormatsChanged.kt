package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraFrameFormatsChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
