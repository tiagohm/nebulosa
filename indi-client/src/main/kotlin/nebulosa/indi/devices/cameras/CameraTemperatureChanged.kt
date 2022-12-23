package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraTemperatureChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
