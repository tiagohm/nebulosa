package nebulosa.indi.devices.filterwheels

import nebulosa.indi.devices.PropertyChangedEvent

data class FilterWheelFilterIsMovingChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
