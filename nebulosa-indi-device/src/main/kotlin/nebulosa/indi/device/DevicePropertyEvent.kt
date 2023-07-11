package nebulosa.indi.device

sealed interface DevicePropertyEvent : DeviceEvent<Device> {

    abstract override val device: Device

    val property: PropertyVector<*, *>
}
