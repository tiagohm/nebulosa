package nebulosa.indi.device

data class DeviceIsConnecting(override val device: Device) : DeviceEvent<Device>
