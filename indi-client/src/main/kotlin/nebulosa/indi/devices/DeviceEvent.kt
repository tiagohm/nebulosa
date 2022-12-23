package nebulosa.indi.devices

interface DeviceEvent<out T : Device> {

    val device: T?
}
