package nebulosa.indi.devices.events

import nebulosa.indi.devices.Device

interface DeviceEvent<out T : Device> {

    val device: T?
}
