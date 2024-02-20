package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelNamesChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
