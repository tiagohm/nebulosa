package nebulosa.indi.device.filterwheels

import nebulosa.indi.device.PropertyChangedEvent

data class FilterWheelPositionChanged(
    override val device: FilterWheel,
    val previous: Int,
) : FilterWheelEvent, PropertyChangedEvent
