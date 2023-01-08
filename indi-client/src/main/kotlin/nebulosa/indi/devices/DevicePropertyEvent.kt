package nebulosa.indi.devices

sealed interface DevicePropertyEvent : DeviceEvent<Device> {

    val property: PropertyVector<*, *>
}
