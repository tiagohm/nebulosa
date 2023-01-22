package nebulosa.indi.device.filterwheels

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelMovingChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
