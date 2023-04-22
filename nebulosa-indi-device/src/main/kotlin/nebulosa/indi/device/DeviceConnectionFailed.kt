package nebulosa.indi.device

data class DeviceConnectionFailed(override val device: Device) : DeviceEvent<Device>
