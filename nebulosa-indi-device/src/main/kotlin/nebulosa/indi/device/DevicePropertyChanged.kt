package nebulosa.indi.device

data class DevicePropertyChanged(override val property: PropertyVector<*, *>) : DevicePropertyEvent
