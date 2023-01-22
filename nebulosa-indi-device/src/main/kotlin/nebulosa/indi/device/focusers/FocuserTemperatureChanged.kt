package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.thermometers.ThermometerEvent

data class FocuserTemperatureChanged(override val device: Focuser) : FocuserEvent, ThermometerEvent<Focuser>, PropertyChangedEvent
