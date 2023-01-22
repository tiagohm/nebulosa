package nebulosa.indi.device.thermometers

data class ThermometerDetached(override val device: Thermometer) : ThermometerEvent<Thermometer>
