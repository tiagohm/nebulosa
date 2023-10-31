package nebulosa.api.services

import nebulosa.indi.device.Device

interface DeviceMessageEvent<T : Device> : MessageEvent {

    val device: T?
}
