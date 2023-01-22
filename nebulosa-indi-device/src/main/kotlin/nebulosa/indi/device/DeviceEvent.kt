package nebulosa.indi.device

interface DeviceEvent<out T : Device> {

    val device: T?
}
