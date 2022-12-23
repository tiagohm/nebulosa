package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent

data class CameraCanSetTemperatureChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
