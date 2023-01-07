package nebulosa.indi.devices.thermometers

import nebulosa.indi.devices.PropertyChangedEvent

data class ThermometerTemperatureChanged(override val device: Thermometer) : ThermometerEvent, PropertyChangedEvent
