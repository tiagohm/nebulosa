package nebulosa.indi.devices

fun interface DeviceEventHandler {

    fun onEventReceived(event: DeviceEvent<*>)
}
