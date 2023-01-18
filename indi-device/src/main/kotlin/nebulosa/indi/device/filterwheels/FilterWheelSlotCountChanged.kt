package nebulosa.indi.device.filterwheels

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelSlotCountChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
