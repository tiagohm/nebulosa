package nebulosa.indi.devices.filterwheels

import nebulosa.indi.devices.PropertyChangedEvent

data class FilterWheelPositionChanged(
    override val device: FilterWheel,
    val previous: Int,
) : FilterWheelEvent, PropertyChangedEvent
