package nebulosa.indi.devices

data class DevicePropertyChanged(
    override val device: Device,
    val property: PropertyVector<*, *>,
) : DeviceEvent<Device>
