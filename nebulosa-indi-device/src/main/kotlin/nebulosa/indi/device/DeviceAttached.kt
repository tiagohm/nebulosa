package nebulosa.indi.device

interface DeviceAttached<T : Device> : DeviceEvent<T> {

    override val device: T
}
