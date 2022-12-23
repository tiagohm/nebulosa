package nebulosa.indi.devices

data class DeviceConnected(override val device: Device) : DeviceEvent<Device>
