package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelMoveFailed(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
