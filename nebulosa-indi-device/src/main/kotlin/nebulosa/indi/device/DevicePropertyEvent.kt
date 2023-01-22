package nebulosa.indi.device

sealed interface DevicePropertyEvent : DeviceEvent<Device> {

    val property: PropertyVector<*, *>
}
