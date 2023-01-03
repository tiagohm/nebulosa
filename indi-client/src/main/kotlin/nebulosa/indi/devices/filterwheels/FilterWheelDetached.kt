package nebulosa.indi.devices.filterwheels

import nebulosa.indi.devices.DeviceEvent

data class FilterWheelDetached(override val device: FilterWheel) : DeviceEvent<FilterWheel>
