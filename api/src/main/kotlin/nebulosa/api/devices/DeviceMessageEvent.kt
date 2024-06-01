package nebulosa.api.devices

import nebulosa.api.messages.MessageEvent
import nebulosa.indi.device.Device

interface DeviceMessageEvent<T : Device> : MessageEvent {

    val device: T?
}
