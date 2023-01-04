package nebulosa.indi.devices.filterwheels

import nebulosa.indi.devices.PropertyChangedEvent

data class FilterWheelIsMoving(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
