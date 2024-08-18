package nebulosa.indi.device.thermometer

import nebulosa.indi.device.DeviceDetached

data class ThermometerDetached(override val device: Thermometer) : ThermometerEvent<Thermometer>, DeviceDetached<Thermometer>
