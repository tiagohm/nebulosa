package nebulosa.indi.device

data class DeviceDisconnected(override val device: Device) : DeviceEvent<Device>
