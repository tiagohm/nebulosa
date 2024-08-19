package nebulosa.indi.device.thermometer

import nebulosa.indi.device.DeviceAttached

data class ThermometerAttached(override val device: Thermometer) : ThermometerEvent<Thermometer>, DeviceAttached<Thermometer>
