package nebulosa.indi.device

interface DeviceEventHandler {

    fun onEventReceived(event: DeviceEvent<*>)

    fun onConnectionClosed()

    fun interface EventReceived : DeviceEventHandler {

        override fun onConnectionClosed() = Unit
    }

    fun interface ConnectionClosed : DeviceEventHandler {

        override fun onEventReceived(event: DeviceEvent<*>) = Unit
    }
}
