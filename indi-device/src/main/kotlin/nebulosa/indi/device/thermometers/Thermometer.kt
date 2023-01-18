package nebulosa.indi.device.thermometers

import nebulosa.indi.device.Device

interface Thermometer : Device {

    val hasThermometer: Boolean

    val temperature: Double
}
