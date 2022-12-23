package nebulosa.indi.devices

data class DeviceDisconnected(override val device: Device) : DeviceEvent<Device>
