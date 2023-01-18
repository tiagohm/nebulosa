package nebulosa.indi.device.cameras

import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.thermometers.ThermometerEvent

data class CameraTemperatureChanged(override val device: Camera) : CameraEvent, ThermometerEvent<Camera>, PropertyChangedEvent
