package nebulosa.indi.devices.filterwheels

import nebulosa.indi.devices.PropertyChangedEvent

data class FilterWheelMoveFailed(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
