package nebulosa.indi.device

sealed interface DevicePropertyEvent : DeviceEvent<Device> {

    val property: PropertyVector<*, *>

    override val device: Device
        get() = property.device
}
