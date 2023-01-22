package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCanSetTemperatureChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
