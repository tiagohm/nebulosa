package nebulosa.indi.device.thermometer

data class ThermometerDetached(override val device: Thermometer) : ThermometerEvent<Thermometer>
