package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent

data class CameraCanSetTemperatureChanged(override val device: Camera) : CameraEvent, PropertyChangedEvent
