package nebulosa.indi.devices

data class DevicePropertyDeleted(
    override val device: Device,
    override val property: PropertyVector<*, *>,
) : DevicePropertyEvent
