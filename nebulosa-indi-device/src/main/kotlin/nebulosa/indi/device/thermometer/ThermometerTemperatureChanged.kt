package nebulosa.indi.device.thermometer

import nebulosa.indi.device.PropertyChangedEvent

data class ThermometerTemperatureChanged(override val device: Thermometer) : ThermometerEvent<Thermometer>, PropertyChangedEvent
