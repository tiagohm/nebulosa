package nebulosa.indi.devices.cameras

import nebulosa.indi.devices.PropertyChangedEvent
import nebulosa.indi.devices.thermometers.ThermometerEvent

data class CameraTemperatureChanged(override val device: Camera) : CameraEvent, ThermometerEvent<Camera>, PropertyChangedEvent
