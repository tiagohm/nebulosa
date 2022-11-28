package nebulosa.indi.devices.events

import nebulosa.indi.devices.Device

data class DeviceDisconnectedEvent(override val device: Device) : DeviceEvent<Device>
