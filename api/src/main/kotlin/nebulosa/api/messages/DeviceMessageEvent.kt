package nebulosa.api.messages

import nebulosa.indi.device.Device

interface DeviceMessageEvent<T : Device> : MessageEvent {

    val device: T?
}
