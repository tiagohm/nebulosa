package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelPositionChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
