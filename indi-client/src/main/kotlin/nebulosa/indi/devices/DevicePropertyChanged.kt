package nebulosa.indi.devices

data class DevicePropertyChanged(
    override val device: Device,
    override val property: PropertyVector<*, *>,
) : DevicePropertyEvent
