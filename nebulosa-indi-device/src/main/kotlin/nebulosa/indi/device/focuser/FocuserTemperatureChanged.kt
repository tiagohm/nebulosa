package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.thermometer.ThermometerEvent

data class FocuserTemperatureChanged(override val device: Focuser) : FocuserEvent, ThermometerEvent<Focuser>, PropertyChangedEvent
