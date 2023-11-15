package nebulosa.api.indi

import nebulosa.api.services.DeviceMessageEvent
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceMessageReceived
import nebulosa.indi.device.DevicePropertyEvent
import nebulosa.indi.device.PropertyVector

data class INDIMessageEvent(
    override val eventName: String,
    override val device: Device? = null,
    val property: PropertyVector<*, *>? = null,
    val message: String? = null,
) : DeviceMessageEvent<Device> {

    constructor(eventName: String, event: DevicePropertyEvent) : this(eventName, event.device, property = event.property)

    constructor(eventName: String, event: DeviceMessageReceived) : this(eventName, event.device, message = event.message)
}
