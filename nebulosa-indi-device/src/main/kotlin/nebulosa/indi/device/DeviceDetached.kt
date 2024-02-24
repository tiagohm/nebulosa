package nebulosa.indi.device

interface DeviceDetached<T : Device> : DeviceEvent<T> {

    override val device: T
}
