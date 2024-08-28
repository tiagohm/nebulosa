package nebulosa.api.focusers

import nebulosa.api.devices.DeviceMessageEvent
import nebulosa.indi.device.focuser.Focuser

data class FocuserMessageEvent(override val eventName: String, override val device: Focuser) : DeviceMessageEvent<Focuser>
