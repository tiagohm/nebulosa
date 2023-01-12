package nebulosa.indi.devices.filterwheels

import nebulosa.indi.devices.PropertyChangedEvent

data class FilterWheelMovingChanged(override val device: FilterWheel) : FilterWheelEvent, PropertyChangedEvent
