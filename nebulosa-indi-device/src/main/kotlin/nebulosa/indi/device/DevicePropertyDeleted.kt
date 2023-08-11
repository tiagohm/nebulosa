package nebulosa.indi.device

data class DevicePropertyDeleted(override val property: PropertyVector<*, *>) : DevicePropertyEvent
