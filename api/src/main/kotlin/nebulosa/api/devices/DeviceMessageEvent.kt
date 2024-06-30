package nebulosa.api.devices

import nebulosa.api.message.MessageEvent
import nebulosa.indi.device.Device

interface DeviceMessageEvent<T : Device> : MessageEvent {

    val device: T?
}
