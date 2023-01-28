package nebulosa.indi.device.thermometer

import nebulosa.indi.device.Device

interface Thermometer : Device {

    val hasThermometer: Boolean

    val temperature: Double
}
