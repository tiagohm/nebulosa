package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraGainMinMaxChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
