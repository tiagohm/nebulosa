package nebulosa.indi.devices.thermometers

import nebulosa.indi.devices.Device

interface Thermometer : Device {

    val temperature: Double
}
