package nebulosa.indi.devices.events

import nebulosa.indi.devices.Device

data class DeviceConnectedEvent(override val device: Device) : DeviceEvent<Device>
