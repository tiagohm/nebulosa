package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraGainMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
