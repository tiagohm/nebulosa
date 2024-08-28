package nebulosa.api.wheels

import nebulosa.api.devices.DeviceMessageEvent
import nebulosa.indi.device.filterwheel.FilterWheel

data class WheelMessageEvent(override val eventName: String, override val device: FilterWheel) : DeviceMessageEvent<FilterWheel>
