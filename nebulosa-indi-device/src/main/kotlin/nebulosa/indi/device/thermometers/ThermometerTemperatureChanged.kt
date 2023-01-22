package nebulosa.indi.device.thermometers

import nebulosa.indi.device.PropertyChangedEvent

data class ThermometerTemperatureChanged(override val device: Thermometer) : ThermometerEvent<Thermometer>, PropertyChangedEvent
