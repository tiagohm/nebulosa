package nebulosa.indi.device.camera

import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.thermometer.ThermometerEvent

data class CameraTemperatureChanged(override val device: Camera) : CameraEvent, ThermometerEvent<Camera>, PropertyChangedEvent
