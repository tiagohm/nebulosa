package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.DeviceAttached

data class FilterWheelAttached(override val device: FilterWheel) : FilterWheelEvent, DeviceAttached<FilterWheel>
