package nebulosa.indi.devices.thermometers

data class ThermometerAttached(override val device: Thermometer) : ThermometerEvent<Thermometer>
