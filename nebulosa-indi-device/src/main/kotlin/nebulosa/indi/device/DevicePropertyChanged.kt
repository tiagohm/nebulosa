package nebulosa.indi.device

data class DevicePropertyChanged(
    override val device: Device,
    override val property: PropertyVector<*, *>,
) : DevicePropertyEvent
