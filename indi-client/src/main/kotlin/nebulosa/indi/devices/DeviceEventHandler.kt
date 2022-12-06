package nebulosa.indi.devices

import nebulosa.indi.devices.events.DeviceEvent

fun interface DeviceEventHandler {

    fun onEventReceived(event: DeviceEvent<*>)
}
