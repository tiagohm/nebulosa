package nebulosa.api.rotators

import nebulosa.api.devices.DeviceMessageEvent
import nebulosa.indi.device.rotator.Rotator

data class RotatorMessageEvent(override val eventName: String, override val device: Rotator) : DeviceMessageEvent<Rotator>
