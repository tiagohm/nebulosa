package nebulosa.api.dustcap

import nebulosa.api.devices.DeviceMessageEvent
import nebulosa.indi.device.dustcap.DustCap

data class DustCapMessageEvent(override val eventName: String, override val device: DustCap) : DeviceMessageEvent<DustCap>
