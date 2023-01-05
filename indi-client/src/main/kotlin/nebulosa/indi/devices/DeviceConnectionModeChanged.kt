package nebulosa.indi.devices

data class DeviceConnectionModeChanged(override val device: Device) : DeviceEvent<Device>
