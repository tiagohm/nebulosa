package nebulosa.indi.device

data class DeviceMessageReceived(
    override val device: Device?,
    val message: String,
) : DeviceEvent<Device>
