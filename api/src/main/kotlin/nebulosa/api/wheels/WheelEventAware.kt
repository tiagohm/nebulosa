package nebulosa.api.wheels

import nebulosa.indi.device.filterwheel.FilterWheelEvent

fun interface WheelEventAware {

    fun handleFilterWheelEvent(event: FilterWheelEvent)
}
