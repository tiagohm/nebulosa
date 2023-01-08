package nebulosa.indi.devices

data class DevicePropertyDeleted(
    override val device: Device,
    val property: PropertyVector<*, *>,
) : DeviceEvent<Device>
