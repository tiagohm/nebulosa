package nebulosa.indi.devices

data class DeviceIsConnecting(override val device: Device) : DeviceEvent<Device>
