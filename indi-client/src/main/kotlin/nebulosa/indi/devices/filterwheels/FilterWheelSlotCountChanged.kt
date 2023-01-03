package nebulosa.indi.devices.filterwheels

import nebulosa.indi.devices.PropertyChangedEvent

data class FilterWheelSlotCountChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
