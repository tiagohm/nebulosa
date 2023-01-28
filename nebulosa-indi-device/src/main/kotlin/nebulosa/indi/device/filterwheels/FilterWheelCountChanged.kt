package nebulosa.indi.device.filterwheels

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelCountChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
