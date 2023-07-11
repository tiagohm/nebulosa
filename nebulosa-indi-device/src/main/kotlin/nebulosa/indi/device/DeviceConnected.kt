package nebulosa.indi.device

data class DeviceConnected(override val device: Device) : DeviceEvent<Device>, ConnectionEvent
