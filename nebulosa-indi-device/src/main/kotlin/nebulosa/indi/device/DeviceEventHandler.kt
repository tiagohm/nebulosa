package nebulosa.indi.device

fun interface DeviceEventHandler {

    fun onEventReceived(event: DeviceEvent<*>)

    fun onConnectionClosed() = Unit
}
