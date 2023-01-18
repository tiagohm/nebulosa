package nebulosa.indi.device.filterwheels

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelMoveFailed(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
