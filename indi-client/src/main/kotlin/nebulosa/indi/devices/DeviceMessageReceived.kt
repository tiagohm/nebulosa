package nebulosa.indi.devices

data class DeviceMessageReceived(
    override val device: Device?,
    val message: String,
) : DeviceEvent<Device>
