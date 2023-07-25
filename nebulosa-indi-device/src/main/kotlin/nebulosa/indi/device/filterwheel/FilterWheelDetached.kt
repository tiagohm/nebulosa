package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.DeviceDetached

data class FilterWheelDetached(override val device: FilterWheel) : FilterWheelEvent, DeviceDetached<FilterWheel>
