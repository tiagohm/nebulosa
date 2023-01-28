package nebulosa.indi.device.thermometer

data class ThermometerAttached(override val device: Thermometer) : ThermometerEvent<Thermometer>
