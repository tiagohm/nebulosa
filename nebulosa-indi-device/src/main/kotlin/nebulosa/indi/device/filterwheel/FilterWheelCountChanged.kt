package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelCountChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
