package nebulosa.indi.device.thermometers

data class ThermometerAttached(override val device: Thermometer) : ThermometerEvent<Thermometer>
