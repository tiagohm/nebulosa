package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraGainChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
