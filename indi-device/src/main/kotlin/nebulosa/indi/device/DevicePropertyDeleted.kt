package nebulosa.indi.device

data class DevicePropertyDeleted(
    override val device: Device,
    override val property: PropertyVector<*, *>,
) : DevicePropertyEvent
