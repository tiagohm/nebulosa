package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelMovingChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
